package com.example.orose.repository;

import com.example.orose.model.CycleBassin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CycleBassinRepository extends JpaRepository<CycleBassin, Long> {
    boolean existsByBassinId(Long bassinId);
    boolean existsByBassinIdAndEstClotureFalse(Long bassinId);
    long countByBassinId(Long bassinId);
    List<CycleBassin> findByEstClotureFalse();
} 
