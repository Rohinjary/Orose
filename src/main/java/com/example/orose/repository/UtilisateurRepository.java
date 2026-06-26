package com.example.orose.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.orose.model.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    @Query("SELECT u FROM Utilisateur u " +
            "JOIN u.roles r " +
            "WHERE r.code = 'TECH'")
    List<Utilisateur> findAllTechniciens();
 
}
