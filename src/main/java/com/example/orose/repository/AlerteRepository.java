package com.example.orose.repository;

import com.example.orose.model.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    // Récupère toutes les alertes non résolues
    List<Alerte> findByEstResolueFalseOrderByDateCreationDesc();
}