package com.arllansantana.springbootjwtauth.controllers;

import com.arllansantana.springbootjwtauth.models.User;
import com.arllansantana.springbootjwtauth.payload.request.ChangePasswordRequest;
import com.arllansantana.springbootjwtauth.payload.request.UserDTO;
import com.arllansantana.springbootjwtauth.payload.response.MessageResponse;
import com.arllansantana.springbootjwtauth.repository.UserRepository;
import com.arllansantana.springbootjwtauth.security.services.UserDetailsImpl;
import com.arllansantana.springbootjwtauth.security.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('Participante') or hasAuthority('Administrador')")
    public ResponseEntity<?> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(convertToDto(user));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('Participante') or hasAuthority('Administrador')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDTO userDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setNome(userDTO.getNome());
        user.setCpf(userDTO.getCpf());
        user.setEmail(userDTO.getEmail());

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuário atualizado com sucesso!"));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAuthority('Participante') or hasAuthority('Administrador')")
    public ResponseEntity<?> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        userRepository.delete(user);

        return ResponseEntity.ok(new MessageResponse("Usuário excluído com sucesso!"));
    }

    private UserDTO convertToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .cpf(user.getCpf())
                .email(user.getEmail())
                .tipoUsuario(user.getTipoUsuario())
                .build();
    }
    @PutMapping("/mudar-senha")
    @PreAuthorize("hasAuthority('Participante') or hasAuthority('Administrador')")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { // Alterado aqui

        if (!request.getNovaSenha().equals(request.getConfirmaNovaSenha())) {
            return ResponseEntity.badRequest().body("As senhas não coincidem.");
        }

        // Obter ID diretamente do userDetails
        Long userId = userDetails.getId();

        userService.changePassword(userId, request.getNovaSenha());

        return ResponseEntity.ok("Senha alterada com sucesso.");
    }



}