package com.example.orose.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import groovy.transform.builder.Builder;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntreeStockDTO {

    @NotNull(message = "L'aliment est obligatoire")
    private Long idAliment;

    @NotNull(message = "Le responsable est obligatoire")
    private Long idResponsable;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantiteKg;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    private BigDecimal prixUnitaire;

    @NotNull(message = "La date de réception est obligatoire")
    private LocalDate dateReception;

    @NotNull(message = "La date d'expiration est obligatoire")
    private LocalDate dateExpiration;
}