package com.arllansantana.springbootjwtauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class Startup {

    public static void main(String[] args) {
        // Define a porta a partir da variável de ambiente PORT
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        // Configura a porta no SpringApplication
        SpringApplication app = new SpringApplication(Startup.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", String.valueOf(port)));
        app.run(args);
    }

}