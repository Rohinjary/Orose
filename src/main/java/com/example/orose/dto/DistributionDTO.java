package com.example.orose.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class DistributionDTO {

    // Champs provenant de ton formulaire
    @NotNull(message = "Le bassin est obligatoire")
    private Long idBassin;

    @NotNull(message = "L'aliment est obligatoire")
    private Long idAliment;

    @NotNull(message = "Le responsable est obligatoire")
    private Long idResponsable;

    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantiteDonneeKg;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "L'heure est obligatoire")
    private LocalTime heure;

    private String observations;

    // Getters et Setters nécessaires pour Thymeleaf
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

    public Long getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Long idResponsable) {
        this.idResponsable = idResponsable;
    }

    public BigDecimal getQuantiteDonneeKg() {
        return quantiteDonneeKg;
    }

    public void setQuantiteDonneeKg(BigDecimal quantiteDonneeKg) {
        this.quantiteDonneeKg = quantiteDonneeKg;
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
}