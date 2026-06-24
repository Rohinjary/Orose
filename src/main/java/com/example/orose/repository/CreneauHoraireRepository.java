package com.example.orose.repository;

import com.example.orose.model.CreneauHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreneauHoraireRepository extends JpaRepository<CreneauHoraire, Integer> {
    
    Optional<CreneauHoraire> findByLibelle(String libelle);
}
