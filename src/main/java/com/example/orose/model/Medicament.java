package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "medicament")
@Data
public class Medicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(nullable = false, length = 20)
    private String unite; // kg, litre, piece

    @Column(name = "seuil_minimum", nullable = false, precision = 10, scale = 2)
    private BigDecimal seuilMinimum;
}