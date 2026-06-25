package com.example.orose.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BassinSuiviDTO {
    private Integer idBassin;
    private String codeBassin;
    private String codeUniqueCycle;
    private Integer idCycleBassinAssoc;
    private LocalDate dateDernierePesee;
    private Integer semaine;
    private BigDecimal poidsMoyenActuel;
    private BigDecimal tauxSurvie;
    private String statutCroissance;
}
