package com.example.orose.dto;

import java.math.BigDecimal;

public class StockDetailDTO {
    private String libelle;
    private BigDecimal quantite;
    private long autonomie;
    private String statut;
    private String cssClass;

    public StockDetailDTO(String libelle, BigDecimal quantite, long autonomie, String statut, String cssClass) {
        this.libelle = libelle;
        this.quantite = quantite;
        this.autonomie = autonomie;
        this.statut = statut;
        this.cssClass = cssClass;
    }

    // Getters indispensables pour Thymeleaf
    public String getLibelle() {
        return libelle;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public long getAutonomie() {
        return autonomie;
    }

    public String getStatut() {
        return statut;
    }

    public String getCssClass() {
        return cssClass;
    }
}