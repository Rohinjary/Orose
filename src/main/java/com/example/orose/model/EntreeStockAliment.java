package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Column(name = "prix_total_ar", nullable = false, precision = 15, scale = 2)
    private BigDecimal prixTotalAr;

    @Column(name = "date_reception", nullable = false)
    private LocalDate dateReception;

    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;
}