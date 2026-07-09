package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lot_crevette")
@Data
@Builder

public class LotCrevette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_lot_unique", nullable = false, unique = true, length = 50)
    private String numeroLotUnique;

    @ManyToOne
    @JoinColumn(name = "id_recolte_declaration", nullable = false)
    private RecolteDeclaration recolteDeclaration;

    @Column(name = "poids_moyen_final_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal poidsMoyenFinalG;

    @Column(name = "taille_moyenne_finale_mm", nullable = false, precision = 10, scale = 2)
    private BigDecimal tailleMoyenneFinaleMm;

    @Column(name = "date_recolte", nullable = false)
    private LocalDate dateRecolte;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;
}