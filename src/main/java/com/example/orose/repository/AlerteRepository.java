package com.example.orose.repository;

import com.example.orose.model.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByModuleSourceAndEstResolueFalseOrderByDateCreationDesc(String moduleSource);

    List<Alerte> findByCycleBassinAssocIdAndEstResolueFalse(Integer idCycleBassinAssoc);

    boolean existsByCycleBassinAssocIdAndTypeAlerteAndEstResolueFalse(
            Integer idCycleBassinAssoc, String typeAlerte);

    List<Alerte> findByEstResolueFalseOrderByNiveauDescDateCreationDesc();
}
