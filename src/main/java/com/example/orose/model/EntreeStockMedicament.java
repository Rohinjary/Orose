package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "entree_stock_medicament")
@Data
public class EntreeStockMedicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_medicament", nullable = false)
    private Medicament medicament;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantite;

    @Column(name = "quantite_restante", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteRestante;

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