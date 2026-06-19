package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "suivi_hebdo_bassin")
@Data
public class SuiviHebdoBassin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_cycle", nullable = false)
    private CycleBassin cycle;

    @Column(name = "date_suivi", nullable = false)
    private LocalDate dateSuivi;

    @Column(name = "semaine_actuelle", nullable = false)
    private Integer semaineActuelle;

    @Column(name = "poids_moyen_gramme", nullable = false, precision = 6, scale = 2)
    private BigDecimal poidsMoyenGramme;

    @Column(name = "taille_moyenne_mm", nullable = false, precision = 10, scale = 2)
    private BigDecimal tailleMoyenneMm;

    @Column(name = "nb_vivants", nullable = false)
    private Integer nbVivants;

    @Column(name = "nb_morts", nullable = false)
    private Integer nbMorts;

    // colonne GENERATED ALWAYS en base -> jamais écrite par JPA
    @Column(name = "biomasse_calculee_kg", insertable = false, updatable = false,
            precision = 10, scale = 2)
    private BigDecimal biomasseCalculeeKg;

    @ManyToOne
    @JoinColumn(name = "id_technicien", nullable = false)
    private Utilisateur technicien;

    @Column(columnDefinition = "TEXT")
    private String notes;
}