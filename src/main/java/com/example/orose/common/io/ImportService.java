package com.example.orose.common.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.orose.common.io.exception.ColonneInvalideException;
import com.example.orose.common.io.exception.DoublonException;
import com.example.orose.common.io.exception.FormatInvalideException;
import com.example.orose.common.io.exception.ValeurManquanteException;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import jakarta.persistence.Id;

@Service
public class ImportService {

    public static void validerColonnes(List<String> colonnesAttendues, List<String> colonnesFichier) {
        for (String attendue : colonnesAttendues) {
            boolean trouve = colonnesFichier.stream()
                    .anyMatch(c -> attendue.equalsIgnoreCase(c.trim()));
            if (!trouve) {
                throw new ColonneInvalideException("Colonne attendue manquante : " + attendue);
            }
        }
    }

    public static void validerChampObligatoire(String nomColonne, String valeur, int numeroLigne) {
        if (valeur == null || valeur.trim().isEmpty()) {
            throw new ValeurManquanteException(nomColonne, numeroLigne);
        }
    }

    /**
     * Un champ est obligatoire si son type est primitif (int, double, etc. ne peuvent être null).
     * Les wrappers et String sont considérés comme optionnels par défaut.
     */
    private static boolean estObligatoire(java.lang.reflect.Field champ) {
        return champ.getType().isPrimitive();
    }

    public static void validerType(String nomColonne, String valeur, Class<?> typeCible, int numeroLigne) {
        try {
            Converter.convert(valeur, typeCible);
        } catch (Exception e) {
            throw new FormatInvalideException(nomColonne, valeur, numeroLigne);
        }
    }

    public static <T> void validerDoublon(List<T> existants, T nouvelObjet, List<Field> champs) throws Exception {
        for (T existant : existants) {
            boolean identique = true;
            for (Field champ : champs) {
                champ.setAccessible(true);
                Object a = champ.get(existant);
                Object b = champ.get(nouvelObjet);
                if (a == null && b == null) continue;
                if (a == null || b == null || !a.equals(b)) { identique = false; break; }
            }
            if (identique) {
                throw new DoublonException("Ligne dans le fichier", "doublon détecté");
            }
        }
    }

    public static <T> List<T> importerCsv(Class<T> classe, MultipartFile fichier, char separateur) throws Exception {
        List<String> entetes = new ArrayList<>();
        List<Map<String, String>> lignes = new ArrayList<>();

        try (Reader reader = new InputStreamReader(fichier.getInputStream())) {
            CSVParser parser = new CSVParserBuilder().withSeparator(separateur).build();
            try (CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build()) {
                String[] tabEntetes = csvReader.readNext();
                if (tabEntetes != null) {
                    for (int i = 0; i < tabEntetes.length; i++) tabEntetes[i] = tabEntetes[i].trim();
                    entetes = Arrays.asList(tabEntetes);
                    String[] ligneLue;
                    while ((ligneLue = csvReader.readNext()) != null) {
                        Map<String, String> ligne = new HashMap<>();
                        for (int i = 0; i < entetes.size(); i++) {
                            String valeur = (i < ligneLue.length) ? ligneLue[i].trim() : "";
                            ligne.put(entetes.get(i), valeur);
                        }
                        lignes.add(ligne);
                    }
                }
            }
        }
        return instanceObjets(classe, entetes, lignes);
    }

    public static <T> List<T> importerExcel(Class<T> classe, MultipartFile fichier, int indexFeuille) throws Exception {
        List<String> entetes = new ArrayList<>();
        List<Map<String, String>> lignes = new ArrayList<>();

        try (InputStream is = fichier.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet feuille = workbook.getSheetAt(indexFeuille);
            Iterator<Row> it = feuille.iterator();
            DataFormatter formateur = new DataFormatter();

            if (it.hasNext()) {
                Row ligneEntete = it.next();
                for (Cell cellule : ligneEntete) entetes.add(formateur.formatCellValue(cellule).trim());
            }

            while (it.hasNext()) {
                Row courante = it.next();
                Map<String, String> ligne = new HashMap<>();
                for (int i = 0; i < entetes.size(); i++) {
                    Cell cellule = courante.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    ligne.put(entetes.get(i), formateur.formatCellValue(cellule).trim());
                }
                lignes.add(ligne);
            }
        }
        return instanceObjets(classe, entetes, lignes);
    }

    public static <T> List<T> instanceObjets(Class<T> classe, List<String> colonnesFichier,
                                             List<Map<String, String>> lignes) throws Exception {
        List<T> resultat = new ArrayList<>();
        List<Field> champs = Arrays.stream(classe.getDeclaredFields())
                .filter(c -> !c.isAnnotationPresent(Id.class) && !c.getName().equalsIgnoreCase("id"))
                .toList();
        List<String> colonnesAttendues = champs.stream().map(Field::getName).toList();
        validerColonnes(colonnesAttendues, colonnesFichier);

        int numeroLigne = 2;
        for (Map<String, String> ligne : lignes) {
            T objet = classe.getDeclaredConstructor().newInstance();
            for (Field champ : champs) {
                champ.setAccessible(true);
                String nom = champ.getName();
                String valeur = null;
                for (Map.Entry<String, String> e : ligne.entrySet()) {
                    if (e.getKey().trim().equalsIgnoreCase(nom)) { valeur = e.getValue(); break; }
                }
                if (estObligatoire(champ)) {
                    validerChampObligatoire(nom, valeur, numeroLigne);
                }
                if (valeur != null && !valeur.trim().isEmpty()) {
                    validerType(nom, valeur, champ.getType(), numeroLigne);
                    champ.set(objet, Converter.convert(valeur, champ.getType()));
                }
            }
            validerDoublon(resultat, objet, champs);
            resultat.add(objet);
            numeroLigne++;
        }
        return resultat;
    }
}
