package com.example.orose.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Future;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EntreeStockAlimentDTO {
    @NotNull
    private Long idAliment;
    @Positive
    private BigDecimal quantiteKg;
    @Positive
    private BigDecimal prixTotalAr;
    @NotNull
    @Future
    private LocalDate dateExpiration;
    @NotNull
    private Long idResponsable;

    // Getters et Setters
    public Long getIdAliment() {
        return idAliment;
    }

    public void setIdAliment(Long idAliment) {
        this.idAliment = idAliment;
    }

    public BigDecimal getQuantiteKg() {
        return quantiteKg;
    }

    public void setQuantiteKg(BigDecimal q) {
        this.quantiteKg = q;
    }

    public BigDecimal getPrixTotalAr() {
        return prixTotalAr;
    }

    public void setPrixTotalAr(BigDecimal p) {
        this.prixTotalAr = p;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate d) {
        this.dateExpiration = d;
    }

    public Long getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Long id) {
        this.idResponsable = id;
    }
}