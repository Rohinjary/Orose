package com.example.orose.dto;

import com.example.orose.model.IncidentSanitaire;
import com.example.orose.model.Traitement;
import lombok.Data;
import java.util.List;

@Data
public class IncidentDetailDTO {
    private IncidentSanitaire incident;
    private List<Traitement> traitements;
}