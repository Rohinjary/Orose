package com.example.orose.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class EntreeStockAlimentDTO {

    // Champs du formulaire Nourrissage
    @NotNull(message = "Le bassin est obligatoire")
    private Long idBassin;

    @NotNull(message = "L'aliment est obligatoire")
    private Long idAliment;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantiteKg;           // ← Correspond à getQuantiteKg()

    @NotNull(message = "Le responsable est obligatoire")
    private Long idResponsable;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    private LocalTime heure;

    private String observations;

    // Champs supplémentaires attendus par le service
    private BigDecimal prixUnitaire;
    private BigDecimal prixTotalAr;
    private LocalDate dateExpiration;

    // ==================== GETTERS & SETTERS ====================

    public Long getIdBassin() {
        return idBassin;
    }

    public void setIdBassin(Long idBassin) {
        this.idBassin = idBassin;
    }

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

    public Long getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Long idResponsable) {
        this.idResponsable = idResponsable;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getPrixTotalAr() {
        return prixTotalAr;
    }

    public void setPrixTotalAr(BigDecimal prixTotalAr) {
        this.prixTotalAr = prixTotalAr;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
}