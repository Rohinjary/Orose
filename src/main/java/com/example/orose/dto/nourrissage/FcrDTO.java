package com.example.orose.dto.nourrissage;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FcrDTO {

    private final String codeCycle;
    private final String codeBassin;
    private final BigDecimal totalAlimentsKg;
    private final BigDecimal biomasseProduiteKg;
    private final BigDecimal fcrReel;
    private final String indicateur;
    private final String cssClass;

    public FcrDTO(BigDecimal totalAlimentsKg, BigDecimal biomasseProduiteKg) {
        this(null, null, totalAlimentsKg, biomasseProduiteKg);
    }

    public FcrDTO(String codeCycle, String codeBassin, BigDecimal totalAlimentsKg, BigDecimal biomasseProduiteKg) {
        this.codeCycle = codeCycle;
        this.codeBassin = codeBassin;
        this.totalAlimentsKg = safe(totalAlimentsKg);
        this.biomasseProduiteKg = safe(biomasseProduiteKg);

        if (this.biomasseProduiteKg.compareTo(BigDecimal.ZERO) > 0) {
            this.fcrReel = this.totalAlimentsKg.divide(this.biomasseProduiteKg, 2, RoundingMode.HALF_UP);
        } else {
            this.fcrReel = BigDecimal.ZERO;
        }

        this.indicateur = determinerIndicateur(this.fcrReel);
        this.cssClass = determinerCssClass(this.indicateur);
    }

    public FcrDTO(String codeCycle, String codeBassin, BigDecimal totalAlimentsKg, BigDecimal biomasseProduiteKg,
            BigDecimal fcrReel) {
        this.codeCycle = codeCycle;
        this.codeBassin = codeBassin;
        this.totalAlimentsKg = safe(totalAlimentsKg);
        this.biomasseProduiteKg = safe(biomasseProduiteKg);
        this.fcrReel = safe(fcrReel);

        this.indicateur = determinerIndicateur(this.fcrReel);
        this.cssClass = determinerCssClass(this.indicateur);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String determinerIndicateur(BigDecimal fcr) {
        if (fcr.compareTo(BigDecimal.valueOf(1.2)) >= 0 && fcr.compareTo(BigDecimal.valueOf(1.5)) <= 0) {
            return "Bon";
        }
        if ((fcr.compareTo(BigDecimal.ONE) >= 0 && fcr.compareTo(BigDecimal.valueOf(1.2)) < 0)
                || (fcr.compareTo(BigDecimal.valueOf(1.5)) > 0 && fcr.compareTo(BigDecimal.valueOf(1.8)) <= 0)) {
            return "Acceptable";
        }
        return "Mauvais";
    }

    private String determinerCssClass(String indicateur) {
        return switch (indicateur) {
            case "Bon" -> "badge-success";
            case "Acceptable" -> "badge-warning";
            default -> "badge-danger";
        };
    }

    public BigDecimal getTotalAlimentsKg() {
        return totalAlimentsKg;
    }

    public String getCodeCycle() {
        return codeCycle;
    }

    public String getCodeBassin() {
        return codeBassin;
    }

    public BigDecimal getBiomasseProduiteKg() {
        return biomasseProduiteKg;
    }

    public BigDecimal getFcrReel() {
        return fcrReel;
    }

    public String getIndicateur() {
        return indicateur;
    }

    public String getCssClass() {
        return cssClass;
    }
}
