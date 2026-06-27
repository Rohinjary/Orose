package com.example.orose.repository.stock;

import com.example.orose.model.MouvementStockAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementStockAlimentRepository extends JpaRepository<MouvementStockAliment, Integer> {
    List<MouvementStockAliment> findAllByOrderByDateMouvementDesc();
}
