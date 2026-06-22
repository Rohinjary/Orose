package com.example.orose.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BassinDetailDTO {
    private Long id;
    private String code;
    private BigDecimal surfaceM2;
    private BigDecimal profondeurMetre;
    private LocalDate miseEnService;
    private String statutCode;
    private String statutLibelle;
    private String badgeCss;
    private String notification;
    private String niveauNotification;
    private CycleResumeDTO cycleEnCours;
    private List<HistoAvecAvantDTO> historiqueRecent;
    private List<String> transitionsAutorisees;
}
