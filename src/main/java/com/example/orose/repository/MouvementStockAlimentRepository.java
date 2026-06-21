package com.example.orose.repository;

import com.example.orose.model.MouvementStockAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementStockAlimentRepository extends JpaRepository<MouvementStockAliment, Integer> {

    // On utilise "entreeAliment" car c'est le nom exact de votre attribut dans l'entité
    @Query("SELECT m FROM MouvementStockAliment m WHERE m.entreeAliment.id = :id")
    List<MouvementStockAliment> findByEntreeAlimentId(@Param("id") Integer id);
}