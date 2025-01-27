package com.arllansantana.springbootjwtauth.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalestraDTO {

    private Long id;
    private String tema;
    private String palestrante;
    private LocalDate data;
    private LocalTime hora;
    private String local;
    private String descricao;
}