package com.example.orose.repository;

import com.example.orose.model.EntreeStockAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EntreeStockAlimentRepository extends JpaRepository<EntreeStockAliment, Long> {
    
    // Récupérer les stocks non épuisés, triés par date d'expiration (pour le FIFO)
    @Query("SELECT e FROM EntreeStockAliment e WHERE e.quantiteRestanteKg > 0 ORDER BY e.dateExpiration ASC")
    List<EntreeStockAliment> findStocksDisponibles();
}