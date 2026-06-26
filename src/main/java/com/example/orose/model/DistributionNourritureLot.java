package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "distribution_nourriture_lot")
@Data
public class DistributionNourritureLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_distribution", nullable = false)
    private DistributionNourriture distribution;

    @ManyToOne
    @JoinColumn(name = "id_entree_aliment", nullable = false)
    private EntreeStockAliment entreeAliment;

    @Column(name = "quantite_piquee_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantitePiqueeKg;
}