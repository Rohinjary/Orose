package com.example.orose.common.io;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Converter {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"));

    private static final List<DateTimeFormatter> DATETIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

    private Converter() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object convert(String valeur, Class<?> typeCible) {
        if (valeur == null || valeur.trim().isEmpty()) {
            return null;
        }
        valeur = valeur.trim();

        if (typeCible == String.class) return valeur;
        if (typeCible == int.class || typeCible == Integer.class) return Integer.parseInt(valeur);
        if (typeCible == long.class || typeCible == Long.class) return Long.parseLong(valeur);
        if (typeCible == double.class || typeCible == Double.class) return Double.parseDouble(valeur.replace(',', '.'));
        if (typeCible == float.class || typeCible == Float.class) return Float.parseFloat(valeur.replace(',', '.'));
        if (typeCible == boolean.class || typeCible == Boolean.class) return parseBoolean(valeur);
        if (typeCible == BigDecimal.class) return new BigDecimal(valeur.replace(',', '.'));
        if (typeCible == LocalDate.class) return parseDate(valeur);
        if (typeCible == LocalDateTime.class) return parseDateTime(valeur);
        if (typeCible.isEnum()) return Enum.valueOf((Class<Enum>) typeCible, valeur.toUpperCase());

        return valeur;
    }

    private static boolean parseBoolean(String v) {
        String s = v.toLowerCase();
        return s.equals("true") || s.equals("1") || s.equals("oui") || s.equals("yes");
    }

    private static LocalDate parseDate(String v) {
        for (DateTimeFormatter f : DATE_FORMATS) {
            try { return LocalDate.parse(v, f); } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Date non reconnue : " + v);
    }

    private static LocalDateTime parseDateTime(String v) {
        for (DateTimeFormatter f : DATETIME_FORMATS) {
            try { return LocalDateTime.parse(v, f); } catch (Exception ignored) {}
        }
        try { return LocalDate.parse(v, DateTimeFormatter.ISO_DATE).atStartOfDay(); } catch (Exception ignored) {}
        throw new IllegalArgumentException("DateTime non reconnu : " + v);
    }
}
