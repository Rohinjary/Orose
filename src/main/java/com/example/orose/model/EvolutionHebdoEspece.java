package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "evolution_hebdo_espece",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_espece", "semaine"}))
@Data
public class EvolutionHebdoEspece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_espece", nullable = false)
    private EspeceCrevette espece;

    @Column(nullable = false)
    private Integer semaine;

    @Column(name = "poids_cible_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal poidsCibleG;

    @Column(name = "taille_cible_mm", nullable = false, precision = 10, scale = 2)
    private BigDecimal tailleCibleMm;
}