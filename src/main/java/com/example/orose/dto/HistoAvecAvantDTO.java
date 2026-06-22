package com.example.orose.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistoAvecAvantDTO {
    private Integer id;
    private String codeBassin;
    private String statutAvantCode;
    private String statutAvantLibelle;
    private String statutAvantBadge;
    private String statutApresCode;
    private String statutApresLibelle;
    private String statutApresBadge;
    private LocalDateTime dateChangement;
    private String utilisateurNom;
    private String motif;
}
