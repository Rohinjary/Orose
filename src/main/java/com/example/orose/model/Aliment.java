package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "aliment")
@Data
public class Aliment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(name = "seuil_minimum_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal seuilMinimumKg;
}