package com.example.orose.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CycleResumeDTO {
    private Integer idCycleBassinAssoc;
    private String codeUniqueCycle;
    private Integer tauxAvancement;
    private Integer semaineActuelle;
    private Integer dureeTotaleSemaines;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private Integer effectifInitial;
    private BigDecimal poidsMoyenActuel;
}
