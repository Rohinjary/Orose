package com.example.orose.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recolte_declaration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecolteDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_cycle_bassin_assoc", nullable = false)
    private CycleBassinAssoc cycleBassinAssoc;

    @Column(name = "recolte_estimee_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal recolteEstimeeKg;

    @Column(name = "recolte_reelle_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal recolteReelleKg;

    @Column(name = "perte_kg", insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal perteKg;

    @Column(name = "taux_survie_percent", insertable = false, updatable = false, precision = 5, scale = 2)
    private BigDecimal tauxSurviePercent;

    @Column(name = "date_declaration", nullable = false)
    private LocalDate dateDeclaration;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
