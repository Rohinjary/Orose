package com.example.orose.dto.nourrissage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;


public interface PlanningJourProjection {

    Integer getIdBassin();
    String getCodeBassin();
    String getNoteBassin();

    Integer getIdCreneau();
    String getCreneauLibelle();

    Integer getIdDistribution();

    LocalDate getDateDistribution();

    LocalTime getHeurePrevue();
    LocalTime getHeureNourrissage();

    BigDecimal getQuantitePrevueKg();
    BigDecimal getQuantiteDonneeKg();

    String getStatutDistribution();

    Boolean getEstValide();
}