package com.example.orose.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BassinGrilleDTO {
    private Long id;
    private String code;
    private String statutCode;
    private String statutLibelle;
    private String badgeCss;
    private String codeUniqueCycle;
    private Integer semaineActuelle;
    private Integer tauxAvancement;
    private BigDecimal poidsMoyenActuel;
    private Integer joursRestants;
    private boolean recoltePossible;
    private boolean bloque;
}
