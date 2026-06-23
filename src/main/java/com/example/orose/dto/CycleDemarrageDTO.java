package com.example.orose.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CycleDemarrageDTO {

    private List<Long> idBassins;       // 3 bassins selectionnes
    private int idEspece;
    private BigDecimal effectifInitial; // par bassin
    private BigDecimal coutPostLarves;  // par bassin
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;

    public List<Long> getIdBassins() { return idBassins; }
    public void setIdBassins(List<Long> idBassins) { this.idBassins = idBassins; }

    public int getIdEspece() { return idEspece; }
    public void setIdEspece(int idEspece) { this.idEspece = idEspece; }

    public BigDecimal getEffectifInitial() { return effectifInitial; }
    public void setEffectifInitial(BigDecimal effectifInitial) { this.effectifInitial = effectifInitial; }

    public BigDecimal getCoutPostLarves() { return coutPostLarves; }
    public void setCoutPostLarves(BigDecimal coutPostLarves) { this.coutPostLarves = coutPostLarves; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
}
