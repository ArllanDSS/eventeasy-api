package com.arllansantana.springbootjwtauth.payload.request;

import com.arllansantana.springbootjwtauth.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private User.TipoUsuario tipoUsuario;
}