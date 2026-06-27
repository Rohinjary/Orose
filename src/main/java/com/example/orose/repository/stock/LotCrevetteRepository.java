package com.example.orose.repository.stock;

import com.example.orose.model.LotCrevette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LotCrevetteRepository extends JpaRepository<LotCrevette, Integer> {
    List<LotCrevette> findAllByOrderByDateRecolteDesc();

    @Query("SELECT COALESCE(SUM(l.biomasseActuelleKg), 0) FROM LotCrevette l")
    BigDecimal sumBiomasseActuelle();

    @Query("SELECT COALESCE(SUM(l.biomasseActuelleKg), 0) FROM LotCrevette l WHERE l.biomasseActuelleKg > 0")
    BigDecimal sumBiomasseDisponible();
}
