package com.arllansantana.springbootjwtauth.controllers;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.arllansantana.springbootjwtauth.payload.request.EventoDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.arllansantana.springbootjwtauth.models.Evento;
import com.arllansantana.springbootjwtauth.models.Inscricao;
import com.arllansantana.springbootjwtauth.models.User;
import com.arllansantana.springbootjwtauth.payload.request.InscricaoDTO;
import com.arllansantana.springbootjwtauth.repository.EventoRepository;
import com.arllansantana.springbootjwtauth.repository.InscricaoRepository;
import com.arllansantana.springbootjwtauth.repository.UserRepository;
import com.arllansantana.springbootjwtauth.security.services.UserDetailsImpl;

@RestController
@RequestMapping("/api/inscricoes")
public class InscricaoController {

    @Autowired
    InscricaoRepository inscricaoRepository;

    @Autowired
    EventoRepository eventoRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('Participante')")
    public ResponseEntity<?> inscreverEvento(@RequestBody InscricaoDTO inscricaoDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User participante = userRepository.findById(userDetails.getId()).orElseThrow();
        Evento evento = eventoRepository.findById(inscricaoDTO.getEventoId()).orElseThrow();

        Inscricao inscricao = new Inscricao();
        inscricao.setParticipante(participante);
        inscricao.setEvento(evento);
        inscricao.setDataHoraInscricao(LocalDateTime.now());

        inscricaoRepository.save(inscricao);

        return ResponseEntity.ok("Inscrição realizada com sucesso!");
    }


    @GetMapping
    @PreAuthorize("hasAuthority('Participante')")
    public ResponseEntity<?> listarInscricoes(Authentication authentication) {
        // Obter o ID do participante a partir da sessão
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long participanteId = userDetails.getId();

        // Buscar as inscrições do participante

        List<Inscricao> inscricoes = inscricaoRepository.findByParticipanteId(participanteId);

        // Mapeando os eventos das inscrições para EventoDTO
        List<EventoDTO> eventosInscritos = inscricoes.stream()
                .map(inscricao -> {
                    Evento evento = inscricao.getEvento();
                    String imagemBase64 = evento.getImagem()!= null? Base64.getEncoder().encodeToString(evento.getImagem()): null;

                    // Mapeamento do Evento para DTO com o ID da inscrição
                    return EventoDTO.builder()
                            .id(evento.getId())
                            .nome(evento.getNome())
                            .data(evento.getData())
                            .hora(evento.getHora())
                            .local(evento.getLocal())
                            .descricao(evento.getDescricao())
                            .quantidadeParticipantes(evento.getQuantidadeParticipantes())
                            .imagem(imagemBase64)
                            .inscricaoId(inscricao.getId()) // Adicionando o ID da inscrição
                            .build();
                })
                .collect(Collectors.toList());

        // Retorna a lista de DTOs com os eventos
        return ResponseEntity.ok(eventosInscritos);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Participante')")
    public ResponseEntity<?> cancelarInscricao(@PathVariable Long id, Authentication authentication) {
        try {
            // Verificar se a inscrição existe
            Inscricao inscricao = inscricaoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Inscrição não encontrada"));

            // Verificar se a inscrição pertence ao participante logado
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long participanteId = userDetails.getId();
            if (!inscricao.getParticipante().getId().equals(participanteId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Você não tem permissão para cancelar esta inscrição.");
            }

            // Remover a inscrição
            inscricaoRepository.delete(inscricao);

            return ResponseEntity.ok("Inscrição cancelada com sucesso!");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


}