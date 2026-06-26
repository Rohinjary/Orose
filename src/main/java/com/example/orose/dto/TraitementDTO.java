package com.example.orose.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TraitementDTO {
    private Integer idIncident;
    private Integer idEntreeMedicament;
    private String dosage;
    private Integer dureeJours;
    private LocalDate dateDebut;
    private BigDecimal quantiteUtilisee;
    private Long idResponsable;
}