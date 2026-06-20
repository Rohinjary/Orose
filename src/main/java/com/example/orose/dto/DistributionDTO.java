package com.example.orose.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class DistributionDTO {
    @NotNull
    private Long idCycle;
    @NotNull
    private Long idEntreeAliment;
    @NotNull
    private Long idCreneau;
    @Positive
    private BigDecimal quantitePrevueKg;
    @Positive
    private BigDecimal quantiteDonneeKg;
    @NotNull
    private Long idResponsable;

    // Getters et Setters
    public Long getIdCycle() {
        return idCycle;
    }

    public void setIdCycle(Long idCycle) {
        this.idCycle = idCycle;
    }

    public Long getIdEntreeAliment() {
        return idEntreeAliment;
    }

    public void setIdEntreeAliment(Long idEntreeAliment) {
        this.idEntreeAliment = idEntreeAliment;
    }

    public Long getIdCreneau() {
        return idCreneau;
    }

    public void setIdCreneau(Long idCreneau) {
        this.idCreneau = idCreneau;
    }

    public BigDecimal getQuantitePrevueKg() {
        return quantitePrevueKg;
    }

    public void setQuantitePrevueKg(BigDecimal q) {
        this.quantitePrevueKg = q;
    }

    public BigDecimal getQuantiteDonneeKg() {
        return quantiteDonneeKg;
    }

    public void setQuantiteDonneeKg(BigDecimal q) {
        this.quantiteDonneeKg = q;
    }

    public Long getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Long id) {
        this.idResponsable = id;
    }
}