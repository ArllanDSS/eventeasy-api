package com.arllansantana.springbootjwtauth.payload.response;


import com.arllansantana.springbootjwtauth.models.User;
import lombok.Data;
import java.util.List;




@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private User.TipoUsuario tipoUsuario; //


    public JwtResponse(String accessToken, Long id, String email, User.TipoUsuario tipoUsuario) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.tipoUsuario = tipoUsuario;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

}