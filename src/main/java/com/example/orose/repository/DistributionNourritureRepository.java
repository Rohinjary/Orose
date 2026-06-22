package com.example.orose.repository;

import com.example.orose.model.CreneauHoraire;
import com.example.orose.model.CycleBassin;
import com.example.orose.model.DistributionNourriture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DistributionNourritureRepository extends JpaRepository<DistributionNourriture, Long> {

    // Récupération des distributions par cycle
    List<DistributionNourriture> findByCycleBassinId(Long idCycle);

    // FIX : Utilisation d'un paramètre nommé :dateLimite pour éviter l'erreur Hibernate
    @Query("SELECT AVG(d.quantiteDonneeKg) FROM DistributionNourriture d WHERE d.dateDistribution >= :dateLimite")
    BigDecimal getConsommationMoyenneQuotidienne(@Param("dateLimite") LocalDate dateLimite);

    // Validation d'unicité pour une distribution par créneau
    boolean existsByCycleAndDateDistributionAndCreneau(CycleBassin cycle, LocalDate dateDistribution, CreneauHoraire creneau);
}