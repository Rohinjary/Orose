package com.example.orose.repository;

import com.example.orose.model.Bassin;

import io.micrometer.common.lang.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BassinRepository extends JpaRepository<Bassin, Long> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @NonNull
    List<Bassin> findByStatutActuel_CodeIn(List<String> codes);
}