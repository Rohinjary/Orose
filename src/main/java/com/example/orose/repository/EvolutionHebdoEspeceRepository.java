package com.example.orose.repository;

import com.example.orose.model.EvolutionHebdoEspece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvolutionHebdoEspeceRepository extends JpaRepository<EvolutionHebdoEspece, Long> {

    List<EvolutionHebdoEspece> findByEspeceIdOrderBySemaineAsc(Integer idEspece);

    Optional<EvolutionHebdoEspece> findByEspeceIdAndSemaine(Integer idEspece, Integer semaine);
}
