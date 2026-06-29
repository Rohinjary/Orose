package com.example.orose.common.filter;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filtrage générique en mémoire d'une liste d'objets selon un GenericFilter.
 * Adapté aux listes de taille modérée (jusqu'à quelques milliers d'éléments).
 * Pour de gros volumes, écrire une vraie JPA Specification.
 */
public final class GenericFilterUtil {

    private GenericFilterUtil() {}

    public static <T> List<T> filtrer(List<T> source, GenericFilter filtre) {
        if (source == null || source.isEmpty() || filtre == null || filtre.isEmpty()) {
            return source;
        }
        Class<?> clazz = source.get(0).getClass();
        Map<String, Field> champsParNom = mapChamps(clazz);

        return source.stream()
                .filter(o -> matchContains(o, filtre, champsParNom))
                .filter(o -> matchEquals(o, filtre, champsParNom))
                .filter(o -> matchDate(o, filtre, champsParNom))
                .collect(Collectors.toList());
    }

    private static Map<String, Field> mapChamps(Class<?> clazz) {
        Map<String, Field> m = new HashMap<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                f.setAccessible(true);
                m.putIfAbsent(f.getName().toLowerCase(), f);
            }
            c = c.getSuperclass();
        }
        return m;
    }

    private static Object valeurChamp(Object obj, String nom, Map<String, Field> champs) {
        Field f = champs.get(nom.toLowerCase());
        if (f == null) return null;
        try { return f.get(obj); } catch (IllegalAccessException e) { return null; }
    }

    private static boolean matchContains(Object o, GenericFilter f, Map<String, Field> champs) {
        for (Map.Entry<String, String> e : f.getTextContains().entrySet()) {
            Object v = valeurChamp(o, e.getKey(), champs);
            if (v == null) return false;
            if (!v.toString().toLowerCase().contains(e.getValue())) return false;
        }
        return true;
    }

    private static boolean matchEquals(Object o, GenericFilter f, Map<String, Field> champs) {
        for (Map.Entry<String, Object> e : f.getEquals().entrySet()) {
            Object v = valeurChamp(o, e.getKey(), champs);
            if (v == null) return false;
            if (!v.toString().equalsIgnoreCase(e.getValue().toString())) return false;
        }
        return true;
    }

    private static boolean matchDate(Object o, GenericFilter f, Map<String, Field> champs) {
        if (f.getDateField() == null) return true;
        Object v = valeurChamp(o, f.getDateField(), champs);
        if (v == null) return false;
        LocalDate d = toLocalDate(v);
        if (d == null) return false;
        if (f.getDateMin() != null && d.isBefore(f.getDateMin())) return false;
        if (f.getDateMax() != null && d.isAfter(f.getDateMax())) return false;
        return true;
    }

    private static LocalDate toLocalDate(Object v) {
        if (v instanceof LocalDate d) return d;
        if (v instanceof LocalDateTime dt) return dt.toLocalDate();
        if (v instanceof java.util.Date d) return d.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return null;
    }
}
