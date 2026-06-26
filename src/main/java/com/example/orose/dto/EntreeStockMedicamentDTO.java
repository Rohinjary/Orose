package com.example.orose.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class EntreeStockMedicamentDTO {
    private Integer idMedicament;
    private BigDecimal quantite;
    private BigDecimal prixTotalAr;
    private LocalDate dateExpiration;
    private Integer idResponsable;
}