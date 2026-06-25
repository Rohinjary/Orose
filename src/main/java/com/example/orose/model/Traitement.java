package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "traitement")
@Data
public class Traitement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Lien vers l'incident qui a déclenché le traitement
    @ManyToOne
    @JoinColumn(name = "id_incident", nullable = false)
    private IncidentSanitaire incident;

    // On cible le Médicament global prescrit (ex: "Vitamine C") 
    // et plus une seule entrée de stock spécifique
    @ManyToOne
    @JoinColumn(name = "id_medicament", nullable = false)
    private Medicament medicament;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Column(name = "duree_jours", nullable = false)
    private Integer dureeJours;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    // Quantité totale consommée pour ce traitement (somme des lots utilisés)
    @Column(name = "quantite_utilisee", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteUtilisee;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Relation vers la table d'association qui contient le détail des lots consommés
    // Le "cascade = CascadeType.ALL" permet d'enregistrer les lots automatiquement 
    // en même temps que le traitement.
    @OneToMany(mappedBy = "traitement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TraitementMedicamentLot> lotsUtilises;
}