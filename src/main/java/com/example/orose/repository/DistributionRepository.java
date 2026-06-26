package com.example.orose.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.orose.model.DistributionNourriture;

@Repository
public interface DistributionRepository extends JpaRepository<DistributionNourriture, Long> {
    List<DistributionNourriture> findByCycleBassinAssocId(Long id);

    @Query(value = "SELECT valider_distribution(:id, :qte)", nativeQuery = true)
    void executerValidation(@Param("id") Long id, @Param("qte") BigDecimal qte);

    @Query("SELECT d FROM DistributionNourriture d " +
            "JOIN d.cycleBassinAssoc cba " +
            "JOIN cba.bassin b " +
            "WHERE (:date IS NULL OR d.dateDistribution = :date) " +
            "AND (:bassinCode IS NULL OR b.code = :bassinCode) " +
            "AND (:cycleId IS NULL OR cba.cycle.id = :cycleId) " +
            "AND (:creneauId IS NULL OR d.creneau.id = :creneauId)")
    List<DistributionNourriture> findByFilters(
            @Param("date") LocalDate date,
            @Param("bassinCode") String bassinCode,
            @Param("cycleId") Long cycleId,
            @Param("creneauId") Long creneauId);
}