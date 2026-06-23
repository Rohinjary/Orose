package com.example.orose.dto;

import lombok.Data;
import java.util.List;

@Data
public class SanitaireDashboardDTO {
    private long nbBassinsSains;
    private long nbBassinsEnTraitement;
    private long nbBassinsQuarantaine;
    private long nbIncidentsActifs;
    private long totalBassins;
    private List<IncidentActifDTO> incidentsActifs;
    private List<BassinEtatDTO> bassinsEtat;
}