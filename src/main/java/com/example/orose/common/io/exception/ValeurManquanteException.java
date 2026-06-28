package com.example.orose.common.io.exception;

public class ValeurManquanteException extends ImportException {
    public ValeurManquanteException(String colonne, int ligne) {
        super("Ligne " + ligne + " : colonne " + colonne + " obligatoire");
    }
}
