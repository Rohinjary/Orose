package com.example.orose.repository;

import com.example.orose.model.EntreeStockMedicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EntreeStockMedicamentRepository extends JpaRepository<EntreeStockMedicament, Integer> {
    List<EntreeStockMedicament> findByQuantiteRestanteGreaterThan(BigDecimal quantite);
}