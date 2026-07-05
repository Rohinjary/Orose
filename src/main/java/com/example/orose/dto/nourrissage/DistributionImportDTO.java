package com.example.orose.dto.nourrissage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO dedie a l'import en masse des distributions de nourriture — il n'existe
 * pas de DTO de creation classique pour ce formulaire (NourrissageController
 * prend des @RequestParam bruts), donc ce DTO existe uniquement pour que
 * l'importeur generique par reflexion ait une forme a plat a instancier.
 */
@Data
public class DistributionImportDTO {

    @NotBlank
    private String codeBassin;

    @NotNull
    private Integer idAliment;

    @NotNull
    private BigDecimal quantiteKg;

    @NotNull
    private LocalDate dateDistribution;

    @NotNull
    private LocalTime heure;
}
