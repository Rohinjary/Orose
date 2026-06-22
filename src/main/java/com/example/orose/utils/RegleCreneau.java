package com.example.orose.utils;

import java.time.LocalTime;

public enum RegleCreneau {
    MATIN(LocalTime.of(6, 0), LocalTime.of(10, 0)),
    MIDI(LocalTime.of(11, 0), LocalTime.of(14, 0)),
    SOIR(LocalTime.of(17, 0), LocalTime.of(20, 0)),
    NUIT(LocalTime.of(22, 0), LocalTime.of(1, 0));

    private final LocalTime debut;
    private final LocalTime fin;

    RegleCreneau(LocalTime debut, LocalTime fin) {
        this.debut = debut;
        this.fin = fin;
    }

    public static String determinerLibelle(LocalTime heure) {
        for (RegleCreneau r : values()) {
            if (estDansIntervalle(heure, r.debut, r.fin)) {
                return r.name();
            }
        }
        return "HORS_CRENEAU"; // Ou gérer une exception
    }

    private static boolean estDansIntervalle(LocalTime h, LocalTime debut, LocalTime fin) {
        if (debut.isBefore(fin)) {
            return !h.isBefore(debut) && h.isBefore(fin);
        } else { // Cas spécial pour la nuit (ex: 22h à 01h)
            return !h.isBefore(debut) || h.isBefore(fin);
        }
    }
}