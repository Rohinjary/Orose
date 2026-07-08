package com.example.orose.dto.stock;

public class StockAlerteDTO {

    private String type;
    private String niveau;
    private String message;
    private Float quantiteRestante;
    private String categorie;

    public StockAlerteDTO() {
    }

    public StockAlerteDTO(String type, String niveau, String message, Float quantiteRestante) {
        this(type, niveau, message, quantiteRestante, null);
    }

    public StockAlerteDTO(String type, String niveau, String message, Float quantiteRestante, String categorie) {
        this.type = type;
        this.niveau = niveau;
        this.message = message;
        this.quantiteRestante = quantiteRestante;
        this.categorie = categorie;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Float getQuantiteRestante() { return quantiteRestante; }
    public void setQuantiteRestante(Float quantiteRestante) { this.quantiteRestante = quantiteRestante; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
}
