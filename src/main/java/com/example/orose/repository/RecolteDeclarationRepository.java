package com.example.orose.repository;

import com.example.orose.model.RecolteDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
}
