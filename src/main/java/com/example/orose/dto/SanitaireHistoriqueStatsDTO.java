package com.example.orose.dto;

import lombok.Data;

@Data
public class SanitaireHistoriqueStatsDTO {
    private long totalIncidents30j;
    private double tauxGuerison;
    private String dernierBassinCode;
    private String dernierIncidentDate;
}
