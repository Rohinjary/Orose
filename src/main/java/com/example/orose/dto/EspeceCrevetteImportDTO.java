package com.example.orose.dto;

import lombok.Data;

/**
 * DTO d'import pour une espèce de crevette.
 * Colonnes attendues : nomScientifique, nomCourant
 */
@Data
public class EspeceCrevetteImportDTO {
    private String nomScientifique;
    private String nomCourant;
}
