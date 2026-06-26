package com.example.orose.repository;

import com.example.orose.model.Traitement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraitementRepository extends JpaRepository<Traitement, Integer> {
    List<Traitement> findByIncidentId(Integer idIncident);
}
