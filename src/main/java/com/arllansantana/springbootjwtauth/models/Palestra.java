package com.arllansantana.springbootjwtauth.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "palestras")
public class Palestra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tema;
    private String palestrante;
    private LocalDate data;
    private LocalTime hora;
    private String local;
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "id_evento")
    private Evento evento;
}