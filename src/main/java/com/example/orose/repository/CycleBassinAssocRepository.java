package com.example.orose.repository;

import com.example.orose.model.CycleBassinAssoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CycleBassinAssocRepository extends JpaRepository<CycleBassinAssoc, Long> {
    boolean existsByBassinIdAndEstClotureFalse(Long bassinId);
    List<CycleBassinAssoc> findByCycleId(Long cycleId);
    List<CycleBassinAssoc> findByEstClotureFalse();
    long countByCycleId(Long cycleId);
}
