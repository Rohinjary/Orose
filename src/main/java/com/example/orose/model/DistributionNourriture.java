package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "distribution_nourriture",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_cycle_bassin_assoc", "date_distribution", "id_creneau"}))
@Data
public class DistributionNourriture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_cycle_bassin_assoc", nullable = false)
    private CycleBassinAssoc cycleBassinAssoc;

    @ManyToOne
    @JoinColumn(name = "id_entree_aliment", nullable = false)
    private EntreeStockAliment entreeAliment;

    @ManyToOne
    @JoinColumn(name = "id_creneau", nullable = false)
    private CreneauHoraire creneau;

    @Column(name = "date_distribution", nullable = false)
    private LocalDate dateDistribution;

    @Column(name = "quantite_prevue_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantitePrevueKg;

    @Column(name = "quantite_donnee_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteDonneeKg;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    @Column(nullable = false, length = 20)
    private String statut;

    @Column(name = "est_valide", nullable = false)
    private Boolean estValide;
}