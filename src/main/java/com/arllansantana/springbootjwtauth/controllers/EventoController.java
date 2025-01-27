package com.arllansantana.springbootjwtauth.controllers;

import com.arllansantana.springbootjwtauth.models.Evento;
import com.arllansantana.springbootjwtauth.models.Palestra;
import com.arllansantana.springbootjwtauth.models.User;
import com.arllansantana.springbootjwtauth.payload.request.EventoDTO;
import com.arllansantana.springbootjwtauth.payload.request.PalestraDTO;
import com.arllansantana.springbootjwtauth.payload.request.UserDTO;
import com.arllansantana.springbootjwtauth.repository.EventoRepository;
import com.arllansantana.springbootjwtauth.repository.UserRepository;
import com.arllansantana.springbootjwtauth.security.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
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
@RequestMapping("/api/eventos")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<Evento> criarEvento(@Valid @RequestBody Evento evento) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User administrador = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        evento.setAdministrador(administrador);

        for (Palestra palestra : evento.getPalestras()) {
            palestra.setEvento(evento);
        }

        return new ResponseEntity<>(eventoRepository.save(evento), HttpStatus.CREATED);
    }

    @GetMapping("/administrador/{administradorId}")
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<List<EventoDTO>> listarEventosCriados(@PathVariable Long administradorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        if (!administradorLogado.getId().equals(administradorId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Evento> eventos = eventoRepository.findByAdministradorId(administradorId);

        List<EventoDTO> eventosDTO = eventos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(eventosDTO, HttpStatus.OK);
    }

    @GetMapping("/{eventoId}")
    public ResponseEntity<EventoDTO> visualizarDetalhesEvento(@PathVariable Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        return new ResponseEntity<>(convertToDto(evento), HttpStatus.OK);
    }

    @PutMapping("/{eventoId}")
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<Evento> editarEvento(@PathVariable Long eventoId, @Valid @RequestBody Evento eventoAtualizado) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (!evento.getAdministrador().equals(administradorLogado)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        evento.setNome(eventoAtualizado.getNome());
        evento.setData(eventoAtualizado.getData());
        evento.setHora(eventoAtualizado.getHora());
        evento.setLocal(eventoAtualizado.getLocal());
        evento.setDescricao(eventoAtualizado.getDescricao());

        return new ResponseEntity<>(eventoRepository.save(evento), HttpStatus.OK);
    }

    @DeleteMapping("/{eventoId}")
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<Void> excluirEvento(@PathVariable Long eventoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (!evento.getAdministrador().equals(administradorLogado)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        eventoRepository.delete(evento);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private EventoDTO convertToDto(Evento evento) {
        return EventoDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .data(evento.getData())
                .hora(evento.getHora())
                .local(evento.getLocal())
                .descricao(evento.getDescricao())
                .quantidadeParticipantes(evento.getQuantidadeParticipantes())
                .administrador(convertToDto(evento.getAdministrador()))
                .palestras(evento.getPalestras().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private UserDTO convertToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .cpf(user.getCpf())
                .email(user.getEmail())
                .tipoUsuario(user.getTipoUsuario())
                .build();
    }

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