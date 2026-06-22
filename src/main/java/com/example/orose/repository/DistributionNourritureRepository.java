package com.example.orose.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.orose.model.CreneauHoraire;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.DistributionNourriture;

@Repository
public interface DistributionNourritureRepository extends JpaRepository<DistributionNourriture, Long> {

    // Utilisation de la nouvelle table pivot
    List<DistributionNourriture> findByCycleBassinAssocId(Integer idCycleBassinAssoc);

    // Moyenne quotidienne
    @Query("SELECT AVG(d.quantiteDonneeKg) FROM DistributionNourriture d WHERE d.dateDistribution >= :dateLimite")
    BigDecimal getConsommationMoyenneQuotidienne(@Param("dateLimite") LocalDate dateLimite);

    // Validation d'unicité : on utilise cycleBassinAssoc au lieu de cycle
    boolean existsByCycleBassinAssocAndDateDistributionAndCreneau(CycleBassinAssoc cycleBassinAssoc,
            LocalDate dateDistribution, CreneauHoraire creneau);
}