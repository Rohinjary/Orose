package com.example.orose.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class IncidentDTO {
    private Integer idCycleBassinAssoc;
    private LocalDate dateDetection;
    private String typeIncident;
    private String description;
    private String niveauGravite;
    private Long idResponsable;
}