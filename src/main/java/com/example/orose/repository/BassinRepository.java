package com.example.orose.repository;

import com.example.orose.model.Bassin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BassinRepository extends JpaRepository<Bassin, Long> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}