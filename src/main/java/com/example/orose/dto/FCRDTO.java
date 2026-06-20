package com.example.orose.dto;

import java.math.BigDecimal;

public class FCRDTO {
    private BigDecimal totalAlimentsDistribues;
    private BigDecimal biomasseProduite;
    private BigDecimal ratioFCR;
    private String appreciation;

    // Constructeur, Getters et Setters
    public FCRDTO(BigDecimal total, BigDecimal biomasse, BigDecimal ratio, String app) {
        this.totalAlimentsDistribues = total;
        this.biomasseProduite = biomasse;
        this.ratioFCR = ratio;
        this.appreciation = app;
    }

    public BigDecimal getTotalAlimentsDistribues() {
        return totalAlimentsDistribues;
    }

    public BigDecimal getBiomasseProduite() {
        return biomasseProduite;
    }

    public BigDecimal getRatioFCR() {
        return ratioFCR;
    }

    public String getAppreciation() {
        return appreciation;
    }
}