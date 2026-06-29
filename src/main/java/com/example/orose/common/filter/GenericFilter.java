package com.example.orose.common.filter;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Conteneur léger de critères pour filtrer une liste côté serveur sans Specification JPA.
 * Utilisé par GenericFilterUtil pour filtrer en mémoire après chargement.
 *
 * Pour les listes volumineuses, préférer un repository custom + Pageable.
 */
public class GenericFilter {

    private final Map<String, String> textContains = new LinkedHashMap<>();
    private final Map<String, Object> equals = new LinkedHashMap<>();
    private String dateField;
    private LocalDate dateMin;
    private LocalDate dateMax;

    public GenericFilter contains(String champ, String valeur) {
        if (valeur != null && !valeur.isBlank()) textContains.put(champ, valeur.toLowerCase());
        return this;
    }

    public GenericFilter eq(String champ, Object valeur) {
        if (valeur != null && !valeur.toString().isBlank()) equals.put(champ, valeur);
        return this;
    }

    public GenericFilter dateBetween(String champ, LocalDate min, LocalDate max) {
        this.dateField = champ;
        this.dateMin = min;
        this.dateMax = max;
        return this;
    }

    public Map<String, String> getTextContains() { return textContains; }
    public Map<String, Object> getEquals() { return equals; }
    public String getDateField() { return dateField; }
    public LocalDate getDateMin() { return dateMin; }
    public LocalDate getDateMax() { return dateMax; }

    public boolean isEmpty() {
        return textContains.isEmpty() && equals.isEmpty() && dateField == null;
    }
}
