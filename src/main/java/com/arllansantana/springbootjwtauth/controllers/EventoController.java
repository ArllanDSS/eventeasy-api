package com.arllansantana.springbootjwtauth.controllers;

import com.arllansantana.springbootjwtauth.models.Evento;
import com.arllansantana.springbootjwtauth.models.Palestra;
import com.arllansantana.springbootjwtauth.models.User;
import com.arllansantana.springbootjwtauth.payload.request.EventoDTO;
import com.arllansantana.springbootjwtauth.payload.request.PalestraDTO;
import com.arllansantana.springbootjwtauth.payload.request.UserDTO;
import com.arllansantana.springbootjwtauth.repository.EventoRepository;
import com.arllansantana.springbootjwtauth.repository.PalestraRepository;
import com.arllansantana.springbootjwtauth.repository.UserRepository;
import com.arllansantana.springbootjwtauth.security.services.UserDetailsServiceImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private static final Logger logger = LoggerFactory.getLogger(EventoController.class);

    @Autowired
    private PalestraRepository palestraRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping
    @PreAuthorize("hasAuthority('Administrador')")
    @Transactional
    public ResponseEntity<Evento> criarEvento(@Valid @RequestBody EventoDTO eventoDTO) {
        logger.info("Criando evento: {}", eventoDTO.getNome());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User administrador = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        Evento evento = new Evento();
        evento.setNome(eventoDTO.getNome());
        evento.setData(eventoDTO.getData());
        evento.setHora(eventoDTO.getHora());
        evento.setLocal(eventoDTO.getLocal());
        evento.setDescricao(eventoDTO.getDescricao());
        evento.setQuantidadeParticipantes(eventoDTO.getQuantidadeParticipantes());
        evento.setAdministrador(administrador);

        if (eventoDTO.getImagem() != null && !eventoDTO.getImagem().isEmpty()) {
            try {
                byte[] imagemBytes = Base64.getDecoder().decode(eventoDTO.getImagem());
                evento.setImagem(imagemBytes);
            } catch (IllegalArgumentException e) {
                logger.error("Erro ao decodificar a imagem", e);
            }
        }

        Evento eventoSalvo = eventoRepository.save(evento);
        logger.info("Evento criado com ID: {}", eventoSalvo.getId());

        return new ResponseEntity<>(eventoSalvo, HttpStatus.CREATED);
    }

    @GetMapping("/administrador/{administradorId}")
    @PreAuthorize("hasAuthority('Administrador')")
    @Transactional
    public ResponseEntity<List<EventoDTO>> listarEventosCriados(@PathVariable Long administradorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        if (!administradorLogado.getId().equals(administradorId)) {
            logger.warn("Tentativa de acesso não autorizado por administrador: {}", email);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Evento> eventos = eventoRepository.findByAdministradorId(administradorId);
        List<EventoDTO> eventosDTO = eventos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(eventosDTO, HttpStatus.OK);
    }

    @GetMapping("/{eventoId}")
    @Transactional
    public ResponseEntity<EventoDTO> visualizarDetalhesEvento(@PathVariable Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        // Recuperar as palestras associadas ao evento
        List<Palestra> palestras = palestraRepository.findByEventoId(eventoId);

        // Converter as palestras para PalestraDTO
        List<PalestraDTO> palestrasDTO = palestras.stream()
                .map(this::convertToDto) // Assume-se que você já tenha um método para converter Palestra para PalestraDTO
                .collect(Collectors.toList());

        // Converter a imagem para base64 (se existir)
        String imagemBase64 = evento.getImagem() != null ? Base64.getEncoder().encodeToString(evento.getImagem()) : null;

        // Criar o EventoDTO com as palestras
        EventoDTO eventoDTO = EventoDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .data(evento.getData())
                .hora(evento.getHora())
                .local(evento.getLocal())
                .descricao(evento.getDescricao())
                .quantidadeParticipantes(evento.getQuantidadeParticipantes())
                .imagem(imagemBase64)
                .palestras(palestrasDTO) // Adicionando as palestras no DTO
                .build();

        return new ResponseEntity<>(eventoDTO, HttpStatus.OK);
    }

    @PutMapping("/{eventoId}")
    @PreAuthorize("hasAuthority('Administrador')")
    @Transactional
    public ResponseEntity<Evento> editarEvento(@PathVariable Long eventoId, @Valid @RequestBody Evento eventoAtualizado) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (!evento.getAdministrador().equals(administradorLogado)) {
            logger.warn("Tentativa de edição não autorizada para o evento ID: {}", eventoId);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        evento.setNome(eventoAtualizado.getNome());
        evento.setData(eventoAtualizado.getData());
        evento.setHora(eventoAtualizado.getHora());
        evento.setLocal(eventoAtualizado.getLocal());
        evento.setDescricao(eventoAtualizado.getDescricao());

        Evento eventoSalvo = eventoRepository.save(evento);
        logger.info("Evento ID: {} atualizado", eventoSalvo.getId());

        return new ResponseEntity<>(eventoSalvo, HttpStatus.OK);
    }

    @DeleteMapping("/{eventoId}")
    @PreAuthorize("hasAuthority('Administrador')")
    @Transactional
    public ResponseEntity<Void> excluirEvento(@PathVariable Long eventoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User administradorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (!evento.getAdministrador().equals(administradorLogado)) {
            logger.warn("Tentativa de exclusão não autorizada para o evento ID: {}", eventoId);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        eventoRepository.delete(evento);
        logger.info("Evento ID: {} excluído com sucesso", eventoId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    @Transactional
    public ResponseEntity<List<EventoDTO>> listarTodosEventos() {
        logger.info("Listando todos os eventos");

        List<Evento> eventos = eventoRepository.findAll();
        List<EventoDTO> eventosDTO = eventos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(eventosDTO, HttpStatus.OK);
    }

    private EventoDTO convertToDto(Evento evento) {
        String imagemBase64 = evento.getImagem() != null ? Base64.getEncoder().encodeToString(evento.getImagem()) : null;

        return EventoDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .data(evento.getData())
                .hora(evento.getHora())
                .local(evento.getLocal())
                .descricao(evento.getDescricao())
                .quantidadeParticipantes(evento.getQuantidadeParticipantes())
                .imagem(imagemBase64) // Adicionando a imagem convertida
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
