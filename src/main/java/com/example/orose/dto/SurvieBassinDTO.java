package com.example.orose.dto;

import java.math.BigDecimal;

public class SurvieBassinDTO {
    private String codeUniqueCycle;
    private String codeBassin;
    private BigDecimal tauxSurvieBassin;
    private BigDecimal tauxSurvieMoyenCycle;
    private String etatBassin;
    private String couleur;
    private Integer rowspanCycle;

    public String getCodeUniqueCycle() {
        return codeUniqueCycle;
    }

    public void setCodeUniqueCycle(String codeUniqueCycle) {
        this.codeUniqueCycle = codeUniqueCycle;
    }

    public String getCodeBassin() {
        return codeBassin;
    }

    public void setCodeBassin(String codeBassin) {
        this.codeBassin = codeBassin;
    }

    public BigDecimal getTauxSurvieBassin() {
        return tauxSurvieBassin;
    }

    public void setTauxSurvieBassin(BigDecimal tauxSurvieBassin) {
        this.tauxSurvieBassin = tauxSurvieBassin;
    }

    public BigDecimal getTauxSurvieMoyenCycle() {
        return tauxSurvieMoyenCycle;
    }

    public void setTauxSurvieMoyenCycle(BigDecimal tauxSurvieMoyenCycle) {
        this.tauxSurvieMoyenCycle = tauxSurvieMoyenCycle;
    }

    public String getEtatBassin() {
        return etatBassin;
    }

    public void setEtatBassin(String etatBassin) {
        this.etatBassin = etatBassin;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public Integer getRowspanCycle() {
        return rowspanCycle;
    }

    public void setRowspanCycle(Integer rowspanCycle) {
        this.rowspanCycle = rowspanCycle;
    }
}
