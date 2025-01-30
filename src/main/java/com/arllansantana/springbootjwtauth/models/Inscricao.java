package com.arllansantana.springbootjwtauth.models;

import java.time.LocalDateTime;


import com.arllansantana.springbootjwtauth.models.Evento;
import com.arllansantana.springbootjwtauth.models.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inscricoes", uniqueConstraints = @UniqueConstraint(columnNames = {"participante_id", "evento_id"}))
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User participante;

    @ManyToOne
    private Evento evento;

    private LocalDateTime dataHoraInscricao;

}
