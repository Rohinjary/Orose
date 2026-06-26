package com.example.orose.repository;

import com.example.orose.model.IncidentSanitaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentSanitaireRepository extends JpaRepository<IncidentSanitaire, Integer> {
    List<IncidentSanitaire> findByCycleBassinAssocId(Integer idCycleBassinAssoc);
    List<IncidentSanitaire> findByEstResoluFalse();
}
