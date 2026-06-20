package com.example.orose.repository;

import com.example.orose.model.CycleBassin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleBassinRepository extends JpaRepository<CycleBassin, Long> {
    boolean existsByBassinId(Long bassinId);
    boolean existsByBassinIdAndEstClotureFalse(Long bassinId);
    long countByBassinId(Long bassinId);
} 
