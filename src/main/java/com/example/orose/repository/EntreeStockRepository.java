package com.example.orose.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.orose.model.EntreeStockAliment;

public interface EntreeStockRepository extends JpaRepository<EntreeStockAliment, Integer> {

    ;

    @Query(value = """
            SELECT COALESCE(SUM(quantite_restante_kg),0)
            FROM entree_stock_aliment
            """, nativeQuery = true)
    Double getStockDisponible();

    @Query(value = """
            SELECT COALESCE(
            AVG(conso_jour),
            0
            )
            FROM (
            SELECT
            date_distribution,
            SUM(quantite_donnee_kg) as conso_jour
            FROM distribution_nourriture
            WHERE statut='NOURRI'
            GROUP BY date_distribution
            ) x
            """, nativeQuery = true)
    Double getConsommationMoyenneJour();

}