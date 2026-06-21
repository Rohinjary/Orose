package com.example.orose.dto;

import com.example.orose.model.Alerte;
import com.example.orose.model.SuiviHebdoBassin;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SuiviBiologiqueDetailDTO {
    private Integer idCycleBassinAssoc;
    private String codeUniqueCycle;
    private String codeBassin;
    private String nomEspece;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private Integer effectifInitial;
    private Integer semaineActuelle;

    private BigDecimal biomassActuelleKg;
    private BigDecimal tauxSurvie;
    private BigDecimal poidsMoyen;
    private BigDecimal tailleMoyenne;
    private boolean calibreAtteint;

    private LocalDate dateEstimeeRecolte;
    private BigDecimal biomasseRecoltableEstimee;

    private List<CourbeCroissanceDTO> courbeReelle;
    private List<CourbeCroissanceDTO> courbeStandard;

    private List<SuiviHebdoBassin> pesees;
    private List<Alerte> alertesActives;
}
