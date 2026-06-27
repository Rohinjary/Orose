package com.example.orose.dto.stock;

import java.time.LocalDate;

public class EntreeStockIntrantDTO {

    private String typeProduit;
    private Integer idProduit;
    private Float quantite;
    private Float prixTotalAr;
    private LocalDate dateReception;
    private LocalDate dateExpiration;
    private Integer idResponsable;

    public String getTypeProduit() {
        return typeProduit;
    }

    public void setTypeProduit(String typeProduit) {
        this.typeProduit = typeProduit;
    }

    public Integer getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(Integer idProduit) {
        this.idProduit = idProduit;
    }

    public Float getQuantite() {
        return quantite;
    }

    public void setQuantite(Float quantite) {
        this.quantite = quantite;
    }

    public Float getPrixTotalAr() {
        return prixTotalAr;
    }

    public void setPrixTotalAr(Float prixTotalAr) {
        this.prixTotalAr = prixTotalAr;
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

    public Integer getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Integer idResponsable) {
        this.idResponsable = idResponsable;
    }
}
