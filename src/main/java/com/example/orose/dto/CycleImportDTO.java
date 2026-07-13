package com.example.orose.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Forme a plat de CycleDemarrageDTO pour l'import en masse : un cycle demarre
 * toujours sur exactement 3 bassins, donc chacun a sa propre colonne plutot
 * qu'une liste (non supportee par l'importeur generique par reflexion).
 */
@Data
public class CycleImportDTO {

    @NotNull
    private Integer idEspece;

    @NotNull
    private BigDecimal effectifInitial;

    @NotNull
    private BigDecimal coutPostLarves;

    @NotNull
    private LocalDate dateDebut;

    @NotNull
    private LocalDate dateFinPrevue;

    @NotNull
    private Long idBassin1;

    @NotNull
    private Long idBassin2;

    @NotNull
    private Long idBassin3;
}
