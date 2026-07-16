package com.example.orose.repository;

import com.example.orose.model.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametreRepository extends JpaRepository<Parametre, Integer> {
    Parametre findByLabel(String label);
}
