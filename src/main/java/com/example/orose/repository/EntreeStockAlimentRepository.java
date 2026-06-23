package com.example.orose.repository;

import com.example.orose.model.EntreeStockAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EntreeStockAlimentRepository extends JpaRepository<EntreeStockAliment, Integer> {
    
    @Query("SELECT COALESCE(SUM(e.quantiteRestanteKg), 0) FROM EntreeStockAliment e")
    BigDecimal sumQuantiteRestante();
    
    List<EntreeStockAliment> findByQuantiteRestanteKgGreaterThan(BigDecimal quantite);
}
