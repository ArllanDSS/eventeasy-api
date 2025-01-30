package com.arllansantana.springbootjwtauth.repository;

import com.arllansantana.springbootjwtauth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arllansantana.springbootjwtauth.models.Inscricao;

import java.util.List;

@Repository
public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    List<Inscricao> findByParticipanteId(Long participanteId);
}