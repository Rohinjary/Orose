package com.example.orose.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreerDistributionDTO {
    private Integer idCycle;
    private Integer idEntreeAliment;
    private String creneau;
    private LocalDate dateDistribution;
    private BigDecimal quantitePrevueKg;
    private Integer idResponsable;

    public CreerDistributionDTO() {}

    public CreerDistributionDTO(Integer idCycle, Integer idEntreeAliment, String creneau, 
                                 LocalDate dateDistribution, BigDecimal quantitePrevueKg, Integer idResponsable) {
        this.idCycle = idCycle;
        this.idEntreeAliment = idEntreeAliment;
        this.creneau = creneau;
        this.dateDistribution = dateDistribution;
        this.quantitePrevueKg = quantitePrevueKg;
        this.idResponsable = idResponsable;
    }

    public Integer getIdCycle() {
        return idCycle;
    }

    public void setIdCycle(Integer idCycle) {
        this.idCycle = idCycle;
    }

    public Integer getIdEntreeAliment() {
        return idEntreeAliment;
    }

    public void setIdEntreeAliment(Integer idEntreeAliment) {
        this.idEntreeAliment = idEntreeAliment;
    }

    public String getCreneau() {
        return creneau;
    }

    public void setCreneau(String creneau) {
        this.creneau = creneau;
    }

    public LocalDate getDateDistribution() {
        return dateDistribution;
    }

    public void setDateDistribution(LocalDate dateDistribution) {
        this.dateDistribution = dateDistribution;
    }

    public BigDecimal getQuantitePrevueKg() {
        return quantitePrevueKg;
    }

    public void setQuantitePrevueKg(BigDecimal quantitePrevueKg) {
        this.quantitePrevueKg = quantitePrevueKg;
    }

    public Integer getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Integer idResponsable) {
        this.idResponsable = idResponsable;
    }
}
