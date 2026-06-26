package com.example.orose.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class IncidentActifDTO {
    private Integer id;
    private String bassinCode;
    private String typeIncident;
    private String gravite;
    private LocalDate dateDetection;
    private String statutTraitement;
}