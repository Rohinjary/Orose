package com.example.orose.dto.stock;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class EntreeStockAlimentDTO {
    public Integer id_aliment;
    public BigDecimal quantite_kg;
    public BigDecimal prix_unitaire;
    public LocalDate date_reception;
    public LocalDate date_expiration;
    public Integer id_utilisateur; 
}
