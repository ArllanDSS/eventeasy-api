package com.arllansantana.springbootjwtauth.repository;

import com.arllansantana.springbootjwtauth.models.Evento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByAdministradorId(Long administradorId);
}