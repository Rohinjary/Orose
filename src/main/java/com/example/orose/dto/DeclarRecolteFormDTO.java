package com.example.orose.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour le formulaire de déclaration de récolte
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeclarRecolteFormDTO {

    private Integer idCycleBassinAssoc;

    private String codeBassin;

    private String codeUniqueCycle;

    private Integer semaineActuelle;

    private Integer effectifInitial;

    /**
     * Récolte estimée = effectif_initial * 0.020 kg
     */
    private BigDecimal recolteEstimeeKg;

    /**
     * Récolte réelle (saisie utilisateur)
     */
    private BigDecimal recolteReelleKg;

    /**
     * Perte calculée (estimée - réelle)
     */
    private BigDecimal perteKg;

    /**
     * Taux de survie en pourcentage
     */
    private BigDecimal tauxSurviePercent;
}
