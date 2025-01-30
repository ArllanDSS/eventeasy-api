package com.arllansantana.springbootjwtauth.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoDTO {

    private Long id;
    private String nome;
    private LocalDate data;
    private LocalTime hora;
    private String local;
    private String descricao;
    private Integer quantidadeParticipantes;
    private UserDTO administrador;
    private List<PalestraDTO> palestras;
    private String imagem; // Base64
}