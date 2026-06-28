package com.example.orose.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * DTO d'import pour un aliment.
 * Colonnes attendues : libelle, seuilMinimumKg
 */
@Data
public class AlimentImportDTO {
    private String libelle;
    private BigDecimal seuilMinimumKg;
}
