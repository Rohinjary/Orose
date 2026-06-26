package com.example.orose.dto.nourrissage;



import java.math.BigDecimal;
import java.time.LocalTime;

public class DashboardDTO {

    private String prochainBassin;
    private LocalTime prochaineHeure;

    private long nbEnAttente;

    private long repasDistribues;
    private long totalRepas;
    private double pourcentageDistribution;

    private BigDecimal stockDisponible;
    private int autonomieJour;
    private boolean stockCritique;

    public String getProchainBassin() {
        return prochainBassin;
    }

    public void setProchainBassin(String prochainBassin) {
        this.prochainBassin = prochainBassin;
    }

    public LocalTime getProchaineHeure() {
        return prochaineHeure;
    }

    public void setProchaineHeure(LocalTime prochaineHeure) {
        this.prochaineHeure = prochaineHeure;
    }

    public long getNbEnAttente() {
        return nbEnAttente;
    }

    public void setNbEnAttente(long nbEnAttente) {
        this.nbEnAttente = nbEnAttente;
    }

    public long getRepasDistribues() {
        return repasDistribues;
    }

    public void setRepasDistribues(long repasDistribues) {
        this.repasDistribues = repasDistribues;
    }

    public long getTotalRepas() {
        return totalRepas;
    }

    public void setTotalRepas(long totalRepas) {
        this.totalRepas = totalRepas;
    }

    public double getPourcentageDistribution() {
        return pourcentageDistribution;
    }

    public void setPourcentageDistribution(double pourcentageDistribution) {
        this.pourcentageDistribution = pourcentageDistribution;
    }

    public BigDecimal getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(BigDecimal stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public int getAutonomieJour() {
        return autonomieJour;
    }

    public void setAutonomieJour(int autonomieJour) {
        this.autonomieJour = autonomieJour;
    }

    public boolean isStockCritique() {
        return stockCritique;
    }

    public void setStockCritique(boolean stockCritique) {
        this.stockCritique = stockCritique;
    }
}