package com.example.orose.dto;

import java.math.BigDecimal;

public class ValiderDistributionDTO {
    private Integer idDistribution;
    private BigDecimal quantiteDonneeKg;
    private Integer idResponsable;

    public ValiderDistributionDTO() {}

    public ValiderDistributionDTO(Integer idDistribution, BigDecimal quantiteDonneeKg, Integer idResponsable) {
        this.idDistribution = idDistribution;
        this.quantiteDonneeKg = quantiteDonneeKg;
        this.idResponsable = idResponsable;
    }

    public Integer getIdDistribution() {
        return idDistribution;
    }

    public void setIdDistribution(Integer idDistribution) {
        this.idDistribution = idDistribution;
    }

    public BigDecimal getQuantiteDonneeKg() {
        return quantiteDonneeKg;
    }

    public void setQuantiteDonneeKg(BigDecimal quantiteDonneeKg) {
        this.quantiteDonneeKg = quantiteDonneeKg;
    }

    public Integer getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Integer idResponsable) {
        this.idResponsable = idResponsable;
    }
}
