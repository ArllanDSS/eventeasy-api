package com.arllansantana.springbootjwtauth.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nome;
    @NotNull
    private LocalDate data;
    @NotNull
    private LocalTime hora;
    @NotNull
    private String local;
    private String descricao;
    @NotNull
    private Integer quantidadeParticipantes;

    @Basic(optional = false,fetch = FetchType.LAZY)
    @Column(name = "imagem", columnDefinition = "bytea")
    private byte[] imagem;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private User administrador;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Palestra> palestras = new ArrayList<>();

    @OneToMany(mappedBy = "evento")
    private List<Inscricao> inscricoes;

}