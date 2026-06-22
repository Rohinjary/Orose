package com.example.orose.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.orose.model.CreneauHoraire;
import java.util.Optional; // <--- AJOUTEZ CET IMPORT

@Repository
public interface CreneauHoraireRepository extends JpaRepository<CreneauHoraire, Integer> {
    Optional<CreneauHoraire> findByLibelle(String libelle);
}