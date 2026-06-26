package com.example.orose.repository;

import com.example.orose.model.MouvementStockMedicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementStockMedicamentRepository extends JpaRepository<MouvementStockMedicament, Integer> {
    List<MouvementStockMedicament> findByEntreeMedicamentId(Integer idEntreeStock);
}