package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "bassin")
@Data
public class Bassin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // B01 ... B09

    @Column(name = "surface_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal surfaceM2;

    @Column(name = "profondeur_metre", nullable = false, precision = 4, scale = 2)
    private BigDecimal profondeurMetre;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "id_statut_actuel", nullable = false)
    private StatutBassin statutActuel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

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

    public BigDecimal getSurfaceM2() {
        return surfaceM2;
    }

    public void setSurfaceM2(BigDecimal surfaceM2) {
        this.surfaceM2 = surfaceM2;
    }

    public BigDecimal getProfondeurMetre() {
        return profondeurMetre;
    }

    public void setProfondeurMetre(BigDecimal profondeurMetre) {
        this.profondeurMetre = profondeurMetre;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public StatutBassin getStatutActuel() {
        return statutActuel;
    }

    public void setStatutActuel(StatutBassin statutActuel) {
        this.statutActuel = statutActuel;
    }
}