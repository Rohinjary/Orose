package com.example.orose.repository.stock;

import com.example.orose.model.MouvementStockCrevette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementStockCrevetteRepository extends JpaRepository<MouvementStockCrevette, Integer> {
    List<MouvementStockCrevette> findAllByOrderByDateMouvementDesc();
    List<MouvementStockCrevette> findByLotCrevetteIdOrderByDateMouvementDesc(Integer lotCrevetteId);
}
