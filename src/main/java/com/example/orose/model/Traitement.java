package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "traitement")
@Data
public class Traitement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_incident", nullable = false)
    private IncidentSanitaire incident;

    @ManyToOne
    @JoinColumn(name = "id_entree_medicament", nullable = false)
    private EntreeStockMedicament entreeMedicament;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Column(name = "duree_jours", nullable = false)
    private Integer dureeJours;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "quantite_utilisee", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteUtilisee;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}