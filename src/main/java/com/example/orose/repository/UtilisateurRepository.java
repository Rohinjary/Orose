package com.example.orose.repository;

import com.example.orose.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    @Query("SELECT u FROM Utilisateur u " +
            "JOIN u.roles r " +
            "WHERE r.code = 'TECH'")
    List<Utilisateur> findAllTechniciens();

}
