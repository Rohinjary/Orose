package com.example.orose.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.orose.model.Cycle;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, Long> {
    List<Cycle> findByEstClotureFalse();

}
