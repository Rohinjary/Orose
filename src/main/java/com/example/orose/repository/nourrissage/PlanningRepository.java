package com.example.orose.repository.nourrissage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.orose.dto.nourrissage.PlanningJourProjection;
import com.example.orose.model.DistributionNourriture;

@Repository
public interface PlanningRepository extends JpaRepository<DistributionNourriture, Integer> {

    @Query(value = """
        SELECT *
        FROM fn_obtenir_ou_creer_planning_du_jour(:idUtilisateur)
        """, nativeQuery = true)
    List<PlanningJourProjection> obtenirOuCreerPlanningDuJour(
            @Param("idUtilisateur") Integer idUtilisateur
    );
}