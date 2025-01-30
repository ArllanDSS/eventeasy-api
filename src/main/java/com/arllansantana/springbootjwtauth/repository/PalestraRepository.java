package com.arllansantana.springbootjwtauth.repository;

import com.arllansantana.springbootjwtauth.models.Palestra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PalestraRepository extends JpaRepository<Palestra, Long> {
    List<Palestra> findByEventoId(Long eventoId);
}
