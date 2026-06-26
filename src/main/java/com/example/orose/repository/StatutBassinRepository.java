package com.example.orose.repository;

import com.example.orose.model.StatutBassin;

import java.lang.StackWalker.Option;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutBassinRepository extends JpaRepository<StatutBassin, Long> {
    Optional<StatutBassin> findByCode(String code);
} 
