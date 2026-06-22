package com.example.orose.repository;

import com.example.orose.model.Aliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlimentRepository extends JpaRepository<Aliment, Integer> {
    // JpaRepository fournit déjà findAll(), findById(), save(), etc.
}