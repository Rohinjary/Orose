package com.example.orose.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class CreneauStatusDTO {
    private String creneau; // MATIN, MIDI, SOIR, NUIT
    private String statut; // EN_ATTENTE, NOURRI, RETARD, RUPTURE
    private LocalTime heurePrevue;
    private LocalTime heureReelle;
    private BigDecimal quantitePrevueKg;
    private BigDecimal quantiteDonneeKg;
    private Boolean estValide;

    // Getters and Setters
    public String getCreneau() {
        return creneau;
    }

    public void setCreneau(String creneau) {
        this.creneau = creneau;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalTime getHeurePrevue() {
        return heurePrevue;
    }

    public void setHeurePrevue(LocalTime heurePrevue) {
        this.heurePrevue = heurePrevue;
    }

    public LocalTime getHeureReelle() {
        return heureReelle;
    }

    public void setHeureReelle(LocalTime heureReelle) {
        this.heureReelle = heureReelle;
    }

    public BigDecimal getQuantitePrevueKg() {
        return quantitePrevueKg;
    }

    public void setQuantitePrevueKg(BigDecimal quantitePrevueKg) {
        this.quantitePrevueKg = quantitePrevueKg;
    }

    public BigDecimal getQuantiteDonneeKg() {
        return quantiteDonneeKg;
    }

    public void setQuantiteDonneeKg(BigDecimal quantiteDonneeKg) {
        this.quantiteDonneeKg = quantiteDonneeKg;
    }

    public Boolean getEstValide() {
        return estValide;
    }

    public void setEstValide(Boolean estValide) {
        this.estValide = estValide;
    }
}
