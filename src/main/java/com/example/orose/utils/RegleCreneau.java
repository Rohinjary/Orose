package com.example.orose.utils;

import java.time.LocalTime;

public enum RegleCreneau {
    MATIN(LocalTime.of(6, 0), LocalTime.of(10, 0), 12.5),
    MIDI(LocalTime.of(11, 0), LocalTime.of(14, 0), 10.0),
    SOIR(LocalTime.of(17, 0), LocalTime.of(20, 0), 10.0),
    NUIT(LocalTime.of(22, 0), LocalTime.of(1, 0), 5.0);

    private final LocalTime debut;
    private final LocalTime fin;
    private final double quantitePrevue;

    RegleCreneau(LocalTime debut, LocalTime fin, double quantitePrevue) {
        this.debut = debut;
        this.fin = fin;
        this.quantitePrevue = quantitePrevue;
    }

    public LocalTime getDebut() {
        return debut;
    }

    public LocalTime getFin() {
        return fin;
    }

    public double getQuantitePrevue() {
        return quantitePrevue;
    }

    public static String determinerLibelle(LocalTime heure) {
        for (RegleCreneau r : values()) {
            if (estDansIntervalle(heure, r.debut, r.fin)) {
                return r.name();
            }
        }
        return "HORS_CRENEAU";
    }

    private static boolean estDansIntervalle(LocalTime h, LocalTime debut, LocalTime fin) {
        if (debut.isBefore(fin)) {
            return !h.isBefore(debut) && h.isBefore(fin);
        } else { // Cas spécial pour la nuit (ex: 22h à 01h)
            return !h.isBefore(debut) || h.isBefore(fin);
        }
    }
}