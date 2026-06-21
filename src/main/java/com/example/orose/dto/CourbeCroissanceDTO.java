package com.example.orose.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourbeCroissanceDTO {
    private Integer semaine;
    private BigDecimal poidsMoyenG;
    private BigDecimal tailleMoyenneMm;
}
