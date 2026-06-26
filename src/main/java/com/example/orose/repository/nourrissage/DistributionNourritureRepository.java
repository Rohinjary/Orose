package com.example.orose.repository.nourrissage;

import com.example.orose.dto.nourrissage.DashboardStatProjection;
import com.example.orose.dto.nourrissage.PlanningJourProjection;
import com.example.orose.model.DistributionNourriture;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DistributionNourritureRepository extends JpaRepository<DistributionNourriture, Long> {

    @Modifying
    @Transactional
    @Query(value = "CALL pr_valider_nourrissage_direct(:idDistribution, :idUtilisateur)", nativeQuery = true)
    void validerNourrissage(@Param("idDistribution") Integer idDistribution,
            @Param("idUtilisateur") Integer idUtilisateur);

    @Query(value = """
            SELECT *
            FROM fn_obtenir_ou_creer_planning_du_jour(:idUtilisateur)
            WHERE statut_distribution IN ('EN_ATTENTE','RETARD')
            ORDER BY heure_prevue
            LIMIT 1
            """, nativeQuery = true)
    PlanningJourProjection findProchainRepas(
            @Param("idUtilisateur") Integer idUtilisateur);

    @Query(value = """
            SELECT COUNT(*)
            FROM fn_obtenir_ou_creer_planning_du_jour(:idUtilisateur)
            WHERE statut_distribution IN ('EN_ATTENTE','RETARD')
            """, nativeQuery = true)
    Long countRepasEnAttente(
            @Param("idUtilisateur") Integer idUtilisateur);

    @Query(value = """
            SELECT
                COUNT(*) as total,
                SUM(
                    CASE
                        WHEN statut_distribution='NOURRI' THEN 1 ELSE 0
                    END
                ) as distribues
            FROM fn_obtenir_ou_creer_planning_du_jour(:idUtilisateur)
            """, nativeQuery = true)
    DashboardStatProjection getStatistiquesPlanning(
            @Param("idUtilisateur") Integer idUtilisateur);

    @Modifying
    @Transactional
    @Query(value = """
            CALL pr_enregistrer_distribution_manuelle(
                :codeBassin,
                :idAliment,
                :quantiteKg,
                :idUtilisateur,
                :dateDistribution,
                :heurePrevue
            )
            """, nativeQuery = true)
    void enregistrerDistributionManuelle(
            @Param("codeBassin") String codeBassin,
            @Param("idAliment") Integer idAliment,
            @Param("quantiteKg") BigDecimal quantiteKg,
            @Param("idUtilisateur") Integer idUtilisateur,
            @Param("dateDistribution") LocalDate dateDistribution,
            @Param("heurePrevue") LocalTime heurePrevue);
}