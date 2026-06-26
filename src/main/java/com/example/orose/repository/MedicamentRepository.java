package com.example.orose.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.orose.model.Medicament;

@Repository
public interface MedicamentRepository extends JpaRepository<Medicament, Integer> {
}