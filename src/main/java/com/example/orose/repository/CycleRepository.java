package com.example.orose.repository;

import com.example.orose.model.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, Long> {
    List<Cycle> findByEstClotureFalse();
    List<Cycle> findByEstClotureFalseAndDateDebutLessThanEqual(LocalDate date);
}
