package com.arllansantana.springbootjwtauth.repository;

import java.util.Optional;

import com.arllansantana.springbootjwtauth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCpf(String cpf);
    Optional<User> findByEmail(String email);

    Boolean existsByCpf(String username);

    Boolean existsByEmail(String email);
}