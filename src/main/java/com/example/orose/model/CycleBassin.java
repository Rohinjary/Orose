package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cycle_bassin")
@Data
public class CycleBassin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code_unique_cycle", nullable = false, unique = true, length = 50)
    private String codeUniqueCycle; // B01-C01-2026

    @ManyToOne
    @JoinColumn(name = "id_bassin", nullable = false)
    private Bassin bassin;

    @ManyToOne
    @JoinColumn(name = "id_espece", nullable = false)
    private EspeceCrevette espece;

    @Column(name = "effectif_initial", nullable = false)
    private Integer effectifInitial;

    @Column(name = "cout_post_larves", nullable = false, precision = 15, scale = 2)
    private BigDecimal coutPostLarves;

    @Column(name = "densite_m2", precision = 10, scale = 2)
    private BigDecimal densiteM2;

    @ManyToOne
    @JoinColumn(name = "id_technicien")
    private Utilisateur technicien;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin_prevue", nullable = false)
    private LocalDate dateFinPrevue;

    @Column(name = "date_fin_reelle")
    private LocalDate dateFinReelle;

    @Column(name = "poids_moyen_actuel", precision = 10, scale = 2)
    private BigDecimal poidsMoyenActuel;

    @Column(name = "semaine_actuelle")
    private Integer semaineActuelle;

    @Column(name = "est_cloture", nullable = false)
    private Boolean estCloture;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}