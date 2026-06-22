package com.example.orose.repository;

import com.example.orose.model.EntreeStockAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<EntreeStockAliment, Long> {
    Optional<EntreeStockAliment> findFirstByAlimentIdOrderByDateReceptionDesc(Long alimentId);
}