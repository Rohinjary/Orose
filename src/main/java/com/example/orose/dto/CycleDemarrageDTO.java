package com.example.orose.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CycleDemarrageDTO {
    private BigDecimal coutPostLarves;
    private BigDecimal effectifInitial;
    private int idEspece;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;

    public BigDecimal getCoutPostLarves() {
        return coutPostLarves;
    }
    public void setCoutPostLarves(BigDecimal coutPostLarves) {
        this.coutPostLarves = coutPostLarves;
    }
    public BigDecimal getEffectifInitial() {
        return effectifInitial;
    }
    public void setEffectifInitial(BigDecimal effectifInitial) {
        this.effectifInitial = effectifInitial;
    }
    public int getIdEspece() {
        return idEspece;
    }
    public void setIdEspece(int idEspece) {
        this.idEspece = idEspece;
    }
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    public LocalDate getDateFinPrevue() {
        return dateFinPrevue;
    }
    public void setDateFinPrevue(LocalDate dateFinPrevue) {
        this.dateFinPrevue = dateFinPrevue;
    }
}
