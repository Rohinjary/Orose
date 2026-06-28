package com.example.orose.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * DTO d'import pour les bassins.
 *
 * Colonnes CSV/Excel attendues (insensibles à la casse) :
 *   code, surfaceM2, profondeurMetre, notes
 *
 * Les champs sans valeur (Object null/empty) sont autorisés
 * (notes en particulier). Le statut et createdAt sont initialisés
 * par le service à "VIDE" et LocalDateTime.now() respectivement.
 */
@Data
public class BassinImportDTO {
    private String code;
    private BigDecimal surfaceM2;
    private BigDecimal profondeurMetre;
    private String notes;
}
