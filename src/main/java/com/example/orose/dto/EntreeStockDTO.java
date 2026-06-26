package com.example.orose.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EntreeStockDTO {

    @NotNull(message = "L'aliment est obligatoire")
    private Long idAliment;
    @NotNull(message = "Le responsable est obligatoire")
    private Long idResponsable;
    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantiteKg;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal prixUnitaire;

    @NotNull(message = "La date de réception est obligatoire")
    private LocalDate dateReception;

    private LocalDate dateExpiration;

    // Getters et Setters...
    public Long getIdAliment() {
        return idAliment;
    }

    public void setIdAliment(Long idAliment) {
        this.idAliment = idAliment;
    }

    public BigDecimal getQuantiteKg() {
        return quantiteKg;
    }

    public void setQuantiteKg(BigDecimal quantiteKg) {
        this.quantiteKg = quantiteKg;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public LocalDate getDateReception() {
        return dateReception;
    }

    public void setDateReception(LocalDate dateReception) {
        this.dateReception = dateReception;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public Long getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Long idResponsable) {
        this.idResponsable = idResponsable;
    }
}