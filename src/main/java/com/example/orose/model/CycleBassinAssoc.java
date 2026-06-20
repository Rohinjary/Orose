package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "cycle_bassin_assoc")
@Data
public class CycleBassinAssoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_cycle", nullable = false)
    private Cycle cycle;

    @ManyToOne
    @JoinColumn(name = "id_bassin", nullable = false)
    private Bassin bassin;

    @Column(name = "effectif_initial", nullable = false)
    private Integer effectifInitial;

    @Column(name = "densite_m2", precision = 10, scale = 2)
    private BigDecimal densiteM2;

    @Column(name = "cout_post_larves", nullable = false, precision = 15, scale = 2)
    private BigDecimal coutPostLarves;

    @Column(name = "poids_moyen_actuel", precision = 10, scale = 2)
    private BigDecimal poidsMoyenActuel = BigDecimal.ZERO;

    @Column(name = "semaine_actuelle")
    private Integer semaineActuelle = 0;

    @Column(name = "date_fin_reelle")
    private java.time.LocalDate dateFinReelle;

    @Column(name = "est_cloture", nullable = false)
    private Boolean estCloture = false;
}
