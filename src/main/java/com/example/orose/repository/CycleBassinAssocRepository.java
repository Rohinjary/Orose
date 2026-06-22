package com.example.orose.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.orose.model.CycleBassinAssoc; // IMPORTANT

@Repository
public interface CycleBassinAssocRepository extends JpaRepository<CycleBassinAssoc, Long> {
    boolean existsByBassinIdAndEstClotureFalse(Long bassinId);
    
    // AJOUTEZ CETTE LIGNE :
    Optional<CycleBassinAssoc> findByBassinIdAndEstClotureFalse(Long bassinId);
    
    List<CycleBassinAssoc> findByCycleId(Long cycleId);
    List<CycleBassinAssoc> findByEstClotureFalse();
    long countByCycleId(Long cycleId);
}