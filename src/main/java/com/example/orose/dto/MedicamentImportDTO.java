package com.example.orose.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * DTO d'import pour un médicament.
 * Colonnes attendues : libelle, unite, seuilMinimum
 */
@Data
public class MedicamentImportDTO {
    private String libelle;
    private String unite;
    private BigDecimal seuilMinimum;
}
