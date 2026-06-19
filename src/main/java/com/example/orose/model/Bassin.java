package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bassin")
@Data
public class Bassin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // B01 ... B09

    @Column(name = "surface_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal surfaceM2;

    @Column(name = "profondeur_metre", nullable = false, precision = 4, scale = 2)
    private BigDecimal profondeurMetre;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "id_statut_actuel", nullable = false)
    private StatutBassin statutActuel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}