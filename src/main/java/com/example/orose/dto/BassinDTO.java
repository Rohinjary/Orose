package com.example.orose.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BassinDTO {
    @NotBlank
    @Pattern(regexp = "B0[1-9]", message = "Le code doit être au format B01 à B09")
    private String code;
    
    @NotNull
    @Positive
    private BigDecimal surface_m2;

    @NotNull
    @Positive
    private BigDecimal profondeur_metre;

    private String notes;

    // Getters et Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getSurface_m2() {
        return surface_m2;
    }

    public void setSurface_m2(BigDecimal surface_m2) {
        this.surface_m2 = surface_m2;
    }

    public BigDecimal getProfondeur_metre() {
        return profondeur_metre;
    }

    public void setProfondeur_metre(BigDecimal profondeur_metre) {
        this.profondeur_metre = profondeur_metre;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
