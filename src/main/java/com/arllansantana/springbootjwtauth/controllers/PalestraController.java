package com.arllansantana.springbootjwtauth.controllers;

import com.arllansantana.springbootjwtauth.models.Evento;
import com.arllansantana.springbootjwtauth.models.Palestra;
import com.arllansantana.springbootjwtauth.models.User;
import com.arllansantana.springbootjwtauth.payload.request.PalestraDTO;
import com.arllansantana.springbootjwtauth.repository.EventoRepository;
import com.arllansantana.springbootjwtauth.repository.PalestraRepository;
import com.arllansantana.springbootjwtauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/palestras")
public class PalestraController {

    @Autowired
    private PalestraRepository palestraRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{eventoId}")
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<Palestra> adicionarPalestra(@PathVariable Long eventoId, @RequestBody Palestra palestra) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (!evento.getAdministrador().equals(administradorLogado)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        palestra.setEvento(evento);
        return new ResponseEntity<>(palestraRepository.save(palestra), HttpStatus.CREATED);
    }

    @PutMapping("/{palestraId}")
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<Palestra> editarPalestra(@PathVariable Long palestraId, @RequestBody Palestra palestraAtualizada) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        Palestra palestra = palestraRepository.findById(palestraId)
                .orElseThrow(() -> new RuntimeException("Palestra não encontrada"));

        if (!palestra.getEvento().getAdministrador().equals(administradorLogado)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        palestra.setTema(palestraAtualizada.getTema());
        palestra.setPalestrante(palestraAtualizada.getPalestrante());
        palestra.setData(palestraAtualizada.getData());
        palestra.setHora(palestraAtualizada.getHora());
        palestra.setLocal(palestraAtualizada.getLocal());
        palestra.setDescricao(palestraAtualizada.getDescricao());

        return new ResponseEntity<>(palestraRepository.save(palestra), HttpStatus.OK);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<PalestraDTO>> listarPalestrasPorEvento(@PathVariable Long eventoId) {
        List<Palestra> palestras = palestraRepository.findByEventoId(eventoId);

        // Mapear a lista de palestras para uma lista de PalestraDTO
        List<PalestraDTO> palestrasDTO = palestras.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(palestrasDTO, HttpStatus.OK);
    }

    @GetMapping("/{palestraId}")
    public ResponseEntity<PalestraDTO> visualizarPalestra(@PathVariable Long palestraId) {
        Palestra palestra = palestraRepository.findById(palestraId)
                .orElseThrow(() -> new RuntimeException("Palestra não encontrada"));

        return new ResponseEntity<>(convertToDto(palestra), HttpStatus.OK);
    }

    @DeleteMapping("/{palestraId}")
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<Void> excluirPalestra(@PathVariable Long palestraId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        Palestra palestra = palestraRepository.findById(palestraId)
                .orElseThrow(() -> new RuntimeException("Palestra não encontrada"));

        if (!palestra.getEvento().getAdministrador().equals(administradorLogado)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        palestraRepository.delete(palestra);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Método auxiliar para converter Palestra para PalestraDTO
    private PalestraDTO convertToDto(Palestra palestra) {
        return PalestraDTO.builder()
                .id(palestra.getId())
                .tema(palestra.getTema())
                .palestrante(palestra.getPalestrante())
                .data(palestra.getData())
                .hora(palestra.getHora())
                .local(palestra.getLocal())
                .descricao(palestra.getDescricao())
                .build();
    }
}