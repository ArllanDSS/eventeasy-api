package com.arllansantana.springbootjwtauth.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "cpf"),
                @UniqueConstraint(columnNames = "email")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor // Construtor com todos os argumentos
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nome;

    @NotBlank
    @Size(max = 11)
    private String cpf;

    @NotBlank
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank
    @Column(name = "senha")
    @Size(max = 120)
    private String password;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipoUsuario;

    public User(String nome, String cpf, String email, String password, TipoUsuario tipoUsuario) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
        this.tipoUsuario = tipoUsuario;
    }

    public enum TipoUsuario {
        Participante,
        Administrador
    }
}