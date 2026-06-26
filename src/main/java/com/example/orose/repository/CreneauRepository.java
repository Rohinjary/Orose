package com.example.orose.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.orose.model.CreneauHoraire;

@Repository
public interface CreneauRepository extends JpaRepository<CreneauHoraire, Integer> {

    List<CreneauHoraire> findAllByOrderByOrdreAsc();
    
    CreneauHoraire findByLibelle(String libelle);
}