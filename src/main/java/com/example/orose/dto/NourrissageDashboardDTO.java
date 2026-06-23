package com.example.orose.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class NourrissageDashboardDTO {
    private LocalDate date;
    private Integer totalBassinsActifs;
    private Integer repasDistribues;
    private Integer repasEnAttente;
    private Integer repasRetard;
    private BigDecimal stockDisponibleKg;
    private Integer autonomieJours;
    private Boolean alerteStock;
    private LocalTime prochainRepasHeure;
    private String prochainRepasBassin;
    private List<BassinDashboardDTO> bassins;

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getTotalBassinsActifs() {
        return totalBassinsActifs;
    }

    public void setTotalBassinsActifs(Integer totalBassinsActifs) {
        this.totalBassinsActifs = totalBassinsActifs;
    }

    public Integer getRepasDistribues() {
        return repasDistribues;
    }

    public void setRepasDistribues(Integer repasDistribues) {
        this.repasDistribues = repasDistribues;
    }

    public Integer getRepasEnAttente() {
        return repasEnAttente;
    }

    public void setRepasEnAttente(Integer repasEnAttente) {
        this.repasEnAttente = repasEnAttente;
    }

    public Integer getRepasRetard() {
        return repasRetard;
    }

    public void setRepasRetard(Integer repasRetard) {
        this.repasRetard = repasRetard;
    }

    public BigDecimal getStockDisponibleKg() {
        return stockDisponibleKg;
    }

    public void setStockDisponibleKg(BigDecimal stockDisponibleKg) {
        this.stockDisponibleKg = stockDisponibleKg;
    }

    public Integer getAutonomieJours() {
        return autonomieJours;
    }

    public void setAutonomieJours(Integer autonomieJours) {
        this.autonomieJours = autonomieJours;
    }

    public Boolean getAlerteStock() {
        return alerteStock;
    }

    public void setAlerteStock(Boolean alerteStock) {
        this.alerteStock = alerteStock;
    }

    public LocalTime getProchainRepasHeure() {
        return prochainRepasHeure;
    }

    public void setProchainRepasHeure(LocalTime prochainRepasHeure) {
        this.prochainRepasHeure = prochainRepasHeure;
    }

    public String getProchainRepasBassin() {
        return prochainRepasBassin;
    }

    public void setProchainRepasBassin(String prochainRepasBassin) {
        this.prochainRepasBassin = prochainRepasBassin;
    }

    public List<BassinDashboardDTO> getBassins() {
        return bassins;
    }

    public void setBassins(List<BassinDashboardDTO> bassins) {
        this.bassins = bassins;
    }

    // Inner class for bassin data
    public static class BassinDashboardDTO {
        private Integer id;
        private String code;
        private String espece;
        private String statutGlobal;
        private CreneauStatusDTO matin;
        private CreneauStatusDTO midi;
        private CreneauStatusDTO soir;
        private CreneauStatusDTO nuit;

        // Getters and Setters
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getEspece() {
            return espece;
        }

        public void setEspece(String espece) {
            this.espece = espece;
        }

        public String getStatutGlobal() {
            return statutGlobal;
        }

        public void setStatutGlobal(String statutGlobal) {
            this.statutGlobal = statutGlobal;
        }

        public CreneauStatusDTO getMatin() {
            return matin;
        }

        public void setMatin(CreneauStatusDTO matin) {
            this.matin = matin;
        }

        public CreneauStatusDTO getMidi() {
            return midi;
        }

        public void setMidi(CreneauStatusDTO midi) {
            this.midi = midi;
        }

        public CreneauStatusDTO getSoir() {
            return soir;
        }

        public void setSoir(CreneauStatusDTO soir) {
            this.soir = soir;
        }

        public CreneauStatusDTO getNuit() {
            return nuit;
        }

        public void setNuit(CreneauStatusDTO nuit) {
            this.nuit = nuit;
        }
    }
}
