package com.example.orose.dto.nourrissage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class JournalDTO {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private Integer id; // CORRIGÉ : Integer au lieu de Long
    private String date;
    private String heure;
    private String codeBassin;
    private String nomAliment;
    private BigDecimal quantite;
    private String nomResponsable;
    private String statut;
    private String cssClass;

    public JournalDTO() {
    }

    // CORRIGÉ : id est maintenant Integer ici aussi
    public JournalDTO(Integer id, LocalDate date, LocalTime heure, String codeBassin,
            String nomAliment, BigDecimal quantite, String nomResponsable, String statut) {
        this.id = id;
        this.date = (date != null) ? date.format(DATE_FORMAT) : "-";
        this.heure = (heure != null) ? heure.format(TIME_FORMAT) : "-";
        this.codeBassin = codeBassin;
        this.nomAliment = (nomAliment != null) ? nomAliment : "Non défini";
        this.quantite = (quantite != null) ? quantite : BigDecimal.ZERO;
        this.nomResponsable = (nomResponsable != null) ? nomResponsable : "Système";
        this.statut = statut;
        this.cssClass = determineCssClass(statut);
    }

    private String determineCssClass(String statut) {
        if (statut == null)
            return "badge-info";
        return switch (statut) {
            case "NOURRI" -> "badge-success";
            case "EN_ATTENTE" -> "badge-warning";
            case "ANNULE" -> "badge-danger";
            default -> "badge-info";
        };
    }

    // Getters et Setters...
    public Integer getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getHeure() {
        return heure;
    }

    public String getCodeBassin() {
        return codeBassin;
    }

    public String getNomAliment() {
        return nomAliment;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public String getNomResponsable() {
        return nomResponsable;
    }

    public String getStatut() {
        return statut;
    }

    public String getCssClass() {
        return cssClass;
    }
}