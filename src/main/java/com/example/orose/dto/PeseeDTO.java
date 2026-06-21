package com.example.orose.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PeseeDTO {
    private Integer idCycleBassinAssoc;
    private LocalDate dateSuivi;
    private BigDecimal poidsMoyenGramme;
    private BigDecimal tailleMoyenneMm;
    private Integer nbVivants;
    private Integer nbMorts;
    private Long idTechnicien;
    private String notes;
}
