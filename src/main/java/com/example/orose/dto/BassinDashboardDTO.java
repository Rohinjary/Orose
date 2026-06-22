package com.example.orose.dto;

import java.util.List;

public class BassinDashboardDTO {
    private Long id;
    private String code;
    private String nomCycle;
    private List<CreneauPlanningDTO> creneaux;

    // Getters
    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNomCycle() {
        return nomCycle;
    }

    public List<CreneauPlanningDTO> getCreneaux() {
        return creneaux;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNomCycle(String nomCycle) {
        this.nomCycle = nomCycle;
    }

    public void setCreneaux(List<CreneauPlanningDTO> creneaux) {
        this.creneaux = creneaux;
    }
}