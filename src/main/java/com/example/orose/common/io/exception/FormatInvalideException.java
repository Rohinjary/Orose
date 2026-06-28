package com.example.orose.common.io.exception;

public class FormatInvalideException extends ImportException {
    public FormatInvalideException(String colonne, String valeur, int ligne) {
        super("Ligne " + ligne + " : valeur '" + valeur + "' invalide pour la colonne " + colonne);
    }
}
