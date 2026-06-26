package com.example.orose.repository;

import com.example.orose.model.Aliment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlimentRepository extends JpaRepository<Aliment, Long> {

    @NonNull
    List<Aliment> findAll();
}
