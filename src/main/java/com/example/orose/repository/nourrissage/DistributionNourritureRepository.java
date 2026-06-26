package com.example.orose.repository.nourrissage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.orose.dto.nourrissage.DashboardStatProjection;
import com.example.orose.dto.nourrissage.PlanningJourProjection;
import com.example.orose.model.DistributionNourriture;

import jakarta.transaction.Transactional;

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
        // --- NOUVELLES MÉTHODES POUR L'HISTORIQUE ET LES STATISTIQUES ---

        // 1. Journal des activités : Affiche le détail du jour
        @Query("SELECT d FROM DistributionNourriture d WHERE d.dateDistribution = :date ORDER BY d.heureNourrissage DESC")
        List<DistributionNourriture> findJournalDuJour(@Param("date") LocalDate date);

        // 2. Historique filtré : Permet de répondre aux filtres Date, Bassin, Cycle
        @Query("SELECT d FROM DistributionNourriture d " +
                        "WHERE (:debut IS NULL OR d.dateDistribution >= :debut) " +
                        "AND (:fin IS NULL OR d.dateDistribution <= :fin) " +
                        "AND (:codeBassin IS NULL OR d.cycleBassinAssoc.bassin.code = :codeBassin) " +
                        "ORDER BY d.dateDistribution DESC, d.heureNourrissage DESC")
        List<DistributionNourriture> findHistoriqueFiltre(
                        @Param("debut") LocalDate debut,
                        @Param("fin") LocalDate fin,
                        @Param("codeBassin") String codeBassin);

        // 3. Statistiques : Consommation totale par bassin sur une période donnée
        @Query("SELECT SUM(d.quantiteDonneeKg) FROM DistributionNourriture d " +
                        "WHERE d.cycleBassinAssoc.bassin.code = :codeBassin " +
                        "AND d.dateDistribution BETWEEN :debut AND :fin")
        BigDecimal sumQuantiteByBassinAndPeriode(
                        @Param("codeBassin") String codeBassin,
                        @Param("debut") LocalDate debut,
                        @Param("fin") LocalDate fin);

        @Query("SELECT d FROM DistributionNourriture d " +
                        "JOIN d.cycleBassinAssoc cba " +
                        "JOIN cba.bassin b " +
                        "WHERE (:date IS NULL OR d.dateDistribution = CAST(:date AS date)) " +
                        "AND (:bassinCode IS NULL OR b.code = CAST(:bassinCode AS text)) " +
                        "AND (:cycleId IS NULL OR cba.cycle.id = CAST(:cycleId AS long)) " +
                        "AND (:creneauId IS NULL OR d.creneau.id = CAST(:creneauId AS long))")
        List<DistributionNourriture> findByFilters(@Param("date") LocalDate date,
                        @Param("bassinCode") String bassinCode,
                        @Param("cycleId") Long cycleId,
                        @Param("creneauId") Long creneauId);

        @Query("SELECT d FROM DistributionNourriture d " +
                        "JOIN FETCH d.cycleBassinAssoc cba " +
                        "JOIN FETCH cba.bassin " +
                        "JOIN FETCH d.aliment " +
                        "JOIN FETCH d.responsable " +
                        "ORDER BY d.dateDistribution DESC, d.heureNourrissage DESC")
        List<DistributionNourriture> findAllJournalComplet();

}