package com.example.orose.repository;

import com.example.orose.model.StatutBassin;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<StatutBassin, Long> {
    Optional<StatutBassin> findByCode(String code);
} 
