package com.example.orose.dto.dashboard;
import lombok.Data;

@Data
public class EtatBassinActifDTO {
    private Integer idBassin;
    private String code;
    private int semaineActuelle;
    private int joursRestants;
    private float poidsMoyen;
    private float avancement;
}
