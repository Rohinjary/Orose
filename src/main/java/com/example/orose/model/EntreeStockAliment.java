package com.example.orose.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "entree_stock_aliment")
@Data
public class EntreeStockAliment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_aliment", nullable = false)
    private Aliment aliment;

    @Column(name = "quantite_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteKg;

    @Column(name = "quantite_restante_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteRestanteKg;

    @Column(name = "prix_unitaire_ar", nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaireAr;

    /*
     * * MISE À JOUR :
     * insertable = false, updatable = false indique à Hibernate de ne jamais
     * inclure cette colonne dans les requêtes INSERT ou UPDATE.
     * C'est la base de données qui gère sa valeur via le TRIGGER/GENERATED COLUMN.
     */
    @Column(name = "prix_total_ar", insertable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal prixTotalAr;

    @Column(name = "date_reception", nullable = false)
    private LocalDate dateReception;

    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;
}