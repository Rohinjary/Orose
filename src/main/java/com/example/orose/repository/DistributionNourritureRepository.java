package com.example.orose.repository;

import com.example.orose.model.DistributionNourriture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DistributionNourritureRepository extends JpaRepository<DistributionNourriture, Long> {
    // Permet de récupérer toutes les distributions d'un cycle donné
    List<DistributionNourriture> findByCycleBassinId(Long idCycle);
}