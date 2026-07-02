package com.example.orose.dto.nourrissage;

import java.math.BigDecimal;

public class FcrLigneDTO extends FcrDTO {

    private final String codeBassin;

    public FcrLigneDTO(String codeBassin, BigDecimal totalAlimentsKg, BigDecimal biomasseProduiteKg) {
        super(totalAlimentsKg, biomasseProduiteKg);
        this.codeBassin = codeBassin;
    }

    public String getCodeBassin() {
        return codeBassin;
    }
}
