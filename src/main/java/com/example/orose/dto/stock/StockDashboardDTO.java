package com.example.orose.dto.stock;

import java.util.List;

public class StockDashboardDTO {

    private Float stockCrevetteKg;
    private Float valeurCrevetteAr;
    private Float stockAlimentKg;
    private Float autonomieAlimentJours;
    private Float stockMedicamentTotal;
    private Integer nbProduitsFaibles;
    private Integer nbLotsPerimes;
    private List<StockAlerteDTO> alertes;

    public Float getStockCrevetteKg() {
        return stockCrevetteKg;
    }

    public void setStockCrevetteKg(Float stockCrevetteKg) {
        this.stockCrevetteKg = stockCrevetteKg;
    }

    public Float getValeurCrevetteAr() {
        return valeurCrevetteAr;
    }

    public void setValeurCrevetteAr(Float valeurCrevetteAr) {
        this.valeurCrevetteAr = valeurCrevetteAr;
    }

    public Float getStockAlimentKg() {
        return stockAlimentKg;
    }

    public void setStockAlimentKg(Float stockAlimentKg) {
        this.stockAlimentKg = stockAlimentKg;
    }

    public Float getAutonomieAlimentJours() {
        return autonomieAlimentJours;
    }

    public void setAutonomieAlimentJours(Float autonomieAlimentJours) {
        this.autonomieAlimentJours = autonomieAlimentJours;
    }

    public Float getStockMedicamentTotal() {
        return stockMedicamentTotal;
    }

    public void setStockMedicamentTotal(Float stockMedicamentTotal) {
        this.stockMedicamentTotal = stockMedicamentTotal;
    }

    public Integer getNbProduitsFaibles() {
        return nbProduitsFaibles;
    }

    public void setNbProduitsFaibles(Integer nbProduitsFaibles) {
        this.nbProduitsFaibles = nbProduitsFaibles;
    }

    public Integer getNbLotsPerimes() {
        return nbLotsPerimes;
    }

    public void setNbLotsPerimes(Integer nbLotsPerimes) {
        this.nbLotsPerimes = nbLotsPerimes;
    }

    public List<StockAlerteDTO> getAlertes() {
        return alertes;
    }

    public void setAlertes(List<StockAlerteDTO> alertes) {
        this.alertes = alertes;
    }
}
