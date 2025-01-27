package com.arllansantana.springbootjwtauth.controllers;

import com.arllansantana.springbootjwtauth.models.User;
import com.arllansantana.springbootjwtauth.payload.request.LoginRequest;
import com.arllansantana.springbootjwtauth.payload.request.SignupRequest;
import com.arllansantana.springbootjwtauth.payload.response.JwtResponse;
import com.arllansantana.springbootjwtauth.payload.response.MessageResponse;
import com.arllansantana.springbootjwtauth.repository.UserRepository;
import com.arllansantana.springbootjwtauth.security.jwt.JwtUtils;
import com.arllansantana.springbootjwtauth.security.services.UserDetailsImpl;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getTipoUsuario())); // Incluído tipoUsuario
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByCpf(signUpRequest.getCpf())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: CPF is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Validação da senha
        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmarSenha())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Password and Confirm Password do not match!"));
        }

        // Cria um novo usuário com as informações do signUpRequest
        User user = new User(signUpRequest.getNome(),
                signUpRequest.getCpf(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getTipoUsuario());

        // Salva o usuário no banco de dados
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}