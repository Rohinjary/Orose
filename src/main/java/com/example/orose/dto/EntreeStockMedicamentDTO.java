package com.example.orose.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EntreeStockMedicamentDTO {
    private Integer idMedicament;
    private BigDecimal quantite;
    private BigDecimal prixTotalAr;
    private LocalDate dateExpiration;
    private Integer idResponsable;
}