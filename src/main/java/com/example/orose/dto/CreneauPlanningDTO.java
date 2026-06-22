package com.example.orose.dto;

public class CreneauPlanningDTO {
    private Long idBassin;
    private Long idCreneau;
    private String nomCreneau;
    private String statut;
    private String infoComplementaire;
    private String quantiteInfo;

    // --- Nouveaux champs pour la dynamique ---
    private String cssClass; // ex: "badge-success", "badge-danger"
    private Long minutesRetard; // ex: 15, 30

    public CreneauPlanningDTO() {
    }

    // Méthode de mise à jour complète
    public void updateDetails(CreneauPlanningDTO source) {
        if (source != null) {
            this.idBassin = source.getIdBassin();
            this.idCreneau = source.getIdCreneau();
            this.nomCreneau = source.getNomCreneau();
            this.statut = source.getStatut();
            this.infoComplementaire = source.getInfoComplementaire();
            this.quantiteInfo = source.getQuantiteInfo();
            this.cssClass = source.getCssClass();
            this.minutesRetard = source.getMinutesRetard();
        }
    }

    // --- Getters & Setters ---
    public Long getIdBassin() {
        return idBassin;
    }

    public void setIdBassin(Long idBassin) {
        this.idBassin = idBassin;
    }

    public Long getIdCreneau() {
        return idCreneau;
    }

    public void setIdCreneau(Long idCreneau) {
        this.idCreneau = idCreneau;
    }

    public String getNomCreneau() {
        return nomCreneau;
    }

    public void setNomCreneau(String nomCreneau) {
        this.nomCreneau = nomCreneau;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getInfoComplementaire() {
        return infoComplementaire;
    }

    public void setInfoComplementaire(String infoComplementaire) {
        this.infoComplementaire = infoComplementaire;
    }

    public String getQuantiteInfo() {
        return quantiteInfo;
    }

    public void setQuantiteInfo(String quantiteInfo) {
        this.quantiteInfo = quantiteInfo;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public Long getMinutesRetard() {
        return minutesRetard;
    }

    public void setMinutesRetard(Long minutesRetard) {
        this.minutesRetard = minutesRetard;
    }

    @Override
    public String toString() {
        return "CreneauPlanningDTO{" + "bassin=" + idBassin + ", creneau=" + idCreneau +
                ", statut='" + statut + '\'' + ", cssClass='" + cssClass + '\'' + '}';
    }
}