package com.example.orose.repository;

import com.example.orose.model.HistoStatutBassin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HistoStatutBassinRepository extends JpaRepository<HistoStatutBassin, Long> {
    List<HistoStatutBassin> findByBassinIdOrderByDateChangementDesc(Long bassinId);
    Optional<HistoStatutBassin> findTopByBassinIdOrderByDateChangementDesc(Long idBassin);

    @Query("SELECT h FROM HistoStatutBassin h " +
           "WHERE h.bassin.id = :idBassin " +
           "AND (:debut IS NULL OR h.dateChangement >= :debut) " +
           "AND (:fin IS NULL OR h.dateChangement <= :fin) " +
           "AND (:typeEtat IS NULL OR h.statutBassin.code = :typeEtat) " +
           "ORDER BY h.dateChangement DESC")
    List<HistoStatutBassin> findHistorique(
            @Param("idBassin") Long idBassin,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("typeEtat") String typeEtat);
}

