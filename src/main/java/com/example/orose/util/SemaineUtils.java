package com.example.orose.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utilitaire centralisant le calcul du numéro de semaine d'un cycle,
 * pour garantir la même convention partout.
 *
 * Convention : S0 = les 7 premiers jours du cycle (jour du démarrage inclus).
 * Jamais négatif (borné à 0 si la date de référence précède le démarrage).
 */
public final class SemaineUtils {

    private SemaineUtils() {
        // classe utilitaire, non instanciable
    }

    /**
     * Calcule le numéro de semaine entre une date de début et une date de référence.
     * Jour J0 à J6 (dateDebut inclus) = semaine 0. Jamais négatif.
     *
     * Exemples :
     *   dateDebut=23/06, dateReference=23/06 → 0 jour écoulé   → S0
     *   dateDebut=23/06, dateReference=29/06 → 6 jours écoulés → S0
     *   dateDebut=23/06, dateReference=30/06 → 7 jours écoulés → S1
     */
    public static int calculer(LocalDate dateDebut, LocalDate dateReference) {
        if (dateDebut == null || dateReference == null) {
            return 0;
        }
        long joursEcoules = ChronoUnit.DAYS.between(dateDebut, dateReference);
        if (joursEcoules < 0) {
            return 0;
        }
        return (int) (joursEcoules / 7);
    }
}