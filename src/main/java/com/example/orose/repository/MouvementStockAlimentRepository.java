package com.example.orose.repository;

import com.example.orose.model.MouvementStockAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MouvementStockAlimentRepository extends JpaRepository<MouvementStockAliment, Long> {
    List<MouvementStockAliment> findByEntreeStockId(Long idEntreeStock);
}