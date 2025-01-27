package com.arllansantana.springbootjwtauth.payload.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String novaSenha;
    private String confirmaNovaSenha;

}