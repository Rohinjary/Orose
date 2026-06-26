package com.example.orose.dto.nourrissage;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningBassinDTO {

    private String codeBassin;
    private String noteBassin;

    private PlanningJourProjection matin;
    private PlanningJourProjection midi;
    private PlanningJourProjection soir;
    private PlanningJourProjection nuit;
}