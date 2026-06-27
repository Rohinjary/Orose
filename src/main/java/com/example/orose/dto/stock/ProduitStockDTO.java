package com.example.orose.dto.stock;

public class ProduitStockDTO {

    private Integer id;
    private String nom;
    private String categorie;
    private Float stockActuel;
    private Float seuilMinimum;
    private String statut;
    private String cssClass;

    public ProduitStockDTO() {
    }

    public ProduitStockDTO(Integer id, String nom, String categorie, Float stockActuel, Float seuilMinimum, String statut, String cssClass) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.stockActuel = stockActuel;
        this.seuilMinimum = seuilMinimum;
        this.statut = statut;
        this.cssClass = cssClass;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Float getStockActuel() {
        return stockActuel;
    }

    public void setStockActuel(Float stockActuel) {
        this.stockActuel = stockActuel;
    }

    public Float getSeuilMinimum() {
        return seuilMinimum;
    }

    public void setSeuilMinimum(Float seuilMinimum) {
        this.seuilMinimum = seuilMinimum;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}
