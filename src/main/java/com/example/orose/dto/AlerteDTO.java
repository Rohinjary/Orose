package com.example.orose.dto;

import java.io.Serializable;

/**
 * DTO pour le transfert des données d'alerte vers la vue (Thymeleaf)
 */
public class AlerteDTO implements Serializable {

    private String message;
    private String niveau; // ROUGE, ORANGE, etc.
    private boolean estCritique;

    // Constructeur par défaut
    public AlerteDTO() {
    }

    // Constructeur complet
    public AlerteDTO(String message, String niveau) {
        this.message = message;
        this.niveau = niveau;
        // Logique métier : une alerte est critique si le niveau est 'ROUGE'
        this.estCritique = "ROUGE".equalsIgnoreCase(niveau);
    }

    // --- Getters et Setters ---

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
        // Recalcul de la criticité en cas de changement de niveau
        this.estCritique = "ROUGE".equalsIgnoreCase(niveau);
    }

    public boolean isEstCritique() {
        return estCritique;
    }

    public void setEstCritique(boolean estCritique) {
        this.estCritique = estCritique;
    }
}