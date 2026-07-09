package com.example.orose.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.orose.model.RecolteDeclaration;

@Repository
public interface RecolteDeclarationRepository extends JpaRepository<RecolteDeclaration, Integer> {

    /**
     * Récupère la dernière déclaration de récolte pour un cycle-bassin
     */
    @Query("select r from RecolteDeclaration r where r.cycleBassinAssoc.id = :idCycleBassinAssoc order by r.id desc")
    Optional<RecolteDeclaration> findByIdCycleBassinAssocIdOrderByIdDesc(@Param("idCycleBassinAssoc") Integer idCycleBassinAssoc);

    /**
     * Récupère toutes les déclarations de récolte pour un cycle-bassin
     */
    @Query("select r from RecolteDeclaration r where r.cycleBassinAssoc.id = :idCycleBassinAssoc")
    List<RecolteDeclaration> findAllByIdCycleBassinAssocId(@Param("idCycleBassinAssoc") Integer idCycleBassinAssoc);

    /**
     * Vérifie si une déclaration existe pour un cycle-bassin
     */
    @Query("select case when count(r) > 0 then true else false end from RecolteDeclaration r where r.cycleBassinAssoc.id = :idCycleBassinAssoc")
    boolean existsByIdCycleBassinAssocId(@Param("idCycleBassinAssoc") Integer idCycleBassinAssoc);

        // fonctions pour le dashboard
    @Query("SELECT COALESCE(SUM(recolte.recolteReelleKg), 0) FROM RecolteDeclaration recolte WHERE YEAR(recolte.dateDeclaration) = :annee")
    float totaleProductionAnnuelle(@Param("annee") int annee);

    @Query("SELECT COALESCE(SUM(recolte.recolteReelleKg), 0) from RecolteDeclaration recolte WHERE YEAR(recolte.dateDeclaration) = :annee AND MONTH(recolte.dateDeclaration) = :mois")
    float getProductionMensuelle(@Param("annee") int annee, @Param("mois") int mois);
}
