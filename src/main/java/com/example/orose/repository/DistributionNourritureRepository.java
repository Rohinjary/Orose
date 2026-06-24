package com.example.orose.repository;

import com.example.orose.model.DistributionNourriture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DistributionNourritureRepository extends JpaRepository<DistributionNourriture, Integer> {
    
    List<DistributionNourriture> findByDateDistribution(LocalDate date);
    
    Optional<DistributionNourriture> findByCycleIdAndDateDistributionAndCreneauId(
        Integer cycleId, LocalDate date, Integer creneauId);
    
    @Query("SELECT d FROM DistributionNourriture d WHERE d.dateDistribution = :date AND d.statut = :statut")
    List<DistributionNourriture> findByDateAndStatut(@Param("date") LocalDate date, @Param("statut") String statut);
}
