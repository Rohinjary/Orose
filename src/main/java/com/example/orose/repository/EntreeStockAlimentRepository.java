package com.example.orose.repository;

import com.example.orose.model.EntreeStockAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EntreeStockAlimentRepository extends JpaRepository<EntreeStockAliment, Long> {

    // Récupérer les stocks non épuisés, triés par date d'expiration (pour le FIFO)
    @Query("SELECT e FROM EntreeStockAliment e WHERE e.quantiteRestanteKg > 0 ORDER BY e.dateExpiration ASC")
    List<EntreeStockAliment> findStocksDisponibles();

    // 1. Somme totale du stock restant (Utilisé par getStockActuelTotal)
    @Query("SELECT SUM(e.quantiteRestanteKg) FROM EntreeStockAliment e")
    BigDecimal sumQuantiteRestante();

    // 2. Valeur financière totale du stock restant (Utilisé par
    // getValeurStockTotale)
    @Query("SELECT SUM(e.quantiteRestanteKg * e.prixUnitaireAr) FROM EntreeStockAliment e")
    BigDecimal sumValeurTotale();

    // 3. Somme des entrées sur les 7 derniers jours (Utilisé par
    // getVariationStockSemaine)
    @Query("SELECT COALESCE(SUM(e.quantiteKg), 0) FROM EntreeStockAliment e WHERE e.dateReception >= :date")
    BigDecimal sumEntreesDepuis(@Param("date") LocalDate date);

    // 4. Stock disponible pour un aliment spécifique (Utilisé par getAlertesStock)
    @Query("SELECT COALESCE(SUM(e.quantiteRestanteKg), 0) FROM EntreeStockAliment e WHERE e.aliment.id = :alimentId")
    BigDecimal sumStockByAliment(@Param("alimentId") Long alimentId);
}