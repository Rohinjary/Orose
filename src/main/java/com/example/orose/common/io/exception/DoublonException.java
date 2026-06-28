package com.example.orose.common.io.exception;

public class DoublonException extends ImportException {
    public DoublonException(String champ, String valeur) {
        super("Valeur déjà existante : " + champ + " = " + valeur);
    }
}
