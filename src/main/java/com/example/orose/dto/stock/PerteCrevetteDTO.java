package com.example.orose.dto.stock;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PerteCrevetteDTO {
    private Integer id;
    private String nomLot;
    private Float quantiteKg;
    private String motif;
    private LocalDate datePerte;
    private String utilisateur;
}
