package com.example.orose.common.io;

import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Export générique CSV / Excel / PDF basé sur la réflexion.
 *
 * Deux modes :
 *  - sans `colonnes` : toutes les colonnes déclarées de la classe, libellées par leur nom Java
 *  - avec `colonnes` (LinkedHashMap ordonnée nom_champ → libellé) : sélectionne et renomme
 */
@Service
public class ExportService {

    private static final String SEPARATEUR_CSV = ";";

    // ─────────── CSV ───────────

    public static <T> void exportCsv(List<T> data, Writer writer) throws Exception {
        exportCsv(data, writer, null);
    }

    public static <T> void exportCsv(List<T> data, Writer writer, LinkedHashMap<String, String> colonnes) throws Exception {
        if (data == null || data.isEmpty()) return;
        List<Field> fields = resoudreChamps(data.get(0).getClass(), colonnes);
        List<String> entetes = resoudreEntetes(fields, colonnes);

        for (int i = 0; i < entetes.size(); i++) {
            writer.write(entetes.get(i));
            writer.write(i != entetes.size() - 1 ? SEPARATEUR_CSV : "\n");
        }
        for (T obj : data) {
            for (int i = 0; i < fields.size(); i++) {
                Object value = fields.get(i).get(obj);
                writer.write(value == null ? "" : value.toString());
                if (i != fields.size() - 1) writer.write(SEPARATEUR_CSV);
            }
            writer.write("\n");
        }
        writer.flush();
    }

    // ─────────── Excel ───────────

    public static <T> void exportExcel(List<T> data, OutputStream outputStream) throws Exception {
        exportExcel(data, outputStream, null);
    }

    public static <T> void exportExcel(List<T> data, OutputStream outputStream, LinkedHashMap<String, String> colonnes) throws Exception {
        if (data == null || data.isEmpty()) return;
        List<Field> fields = resoudreChamps(data.get(0).getClass(), colonnes);
        List<String> entetes = resoudreEntetes(fields, colonnes);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Données");

            Row header = sheet.createRow(0);
            for (int i = 0; i < entetes.size(); i++) {
                header.createCell(i).setCellValue(entetes.get(i));
            }
            for (int r = 0; r < data.size(); r++) {
                Row row = sheet.createRow(r + 1);
                T obj = data.get(r);
                for (int c = 0; c < fields.size(); c++) {
                    Object value = fields.get(c).get(obj);
                    row.createCell(c).setCellValue(value == null ? "" : value.toString());
                }
            }
            for (int i = 0; i < entetes.size(); i++) sheet.autoSizeColumn(i);
            workbook.write(outputStream);
        }
    }

    // ─────────── PDF ───────────

    public static <T> void exportPdf(List<T> data, OutputStream outputStream, String titre) throws Exception {
        exportPdf(data, outputStream, titre, null);
    }

    public static <T> void exportPdf(List<T> data, OutputStream outputStream, String titre,
                                      LinkedHashMap<String, String> colonnes) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph title = new Paragraph(titre == null ? "Export" : titre, fontTitre);
        title.setSpacingAfter(15);
        document.add(title);

        if (data == null || data.isEmpty()) {
            document.add(new Paragraph("(aucune donnée)"));
            document.close();
            return;
        }

        List<Field> fields = resoudreChamps(data.get(0).getClass(), colonnes);
        List<String> entetes = resoudreEntetes(fields, colonnes);

        PdfPTable table = new PdfPTable(entetes.size());
        table.setWidthPercentage(100);

        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String h : entetes) {
            PdfPCell cell = new PdfPCell(new Paragraph(h, fontHeader));
            cell.setBackgroundColor(new java.awt.Color(220, 220, 220));
            table.addCell(cell);
        }
        Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (T obj : data) {
            for (Field f : fields) {
                Object value = f.get(obj);
                table.addCell(new PdfPCell(new Paragraph(value == null ? "" : value.toString(), fontCell)));
            }
        }
        document.add(table);
        document.close();
    }

    // ─────────── Helpers ───────────

    /**
     * Si `colonnes` est nulle ou vide, retourne tous les champs déclarés dans leur ordre.
     * Sinon, retourne seulement les champs nommés dans la map, dans l'ordre de la map.
     */
    private static List<Field> resoudreChamps(Class<?> clazz, LinkedHashMap<String, String> colonnes) {
        Field[] declared = clazz.getDeclaredFields();
        if (colonnes == null || colonnes.isEmpty()) {
            List<Field> tous = new ArrayList<>();
            for (Field f : declared) { f.setAccessible(true); tous.add(f); }
            return tous;
        }
        Map<String, Field> parNom = new LinkedHashMap<>();
        for (Field f : declared) { f.setAccessible(true); parNom.put(f.getName(), f); }
        List<Field> resultat = new ArrayList<>();
        for (String nom : colonnes.keySet()) {
            Field f = parNom.get(nom);
            if (f != null) resultat.add(f);
        }
        return resultat;
    }

    private static List<String> resoudreEntetes(List<Field> fields, LinkedHashMap<String, String> colonnes) {
        List<String> entetes = new ArrayList<>();
        for (Field f : fields) {
            String libelle = (colonnes != null) ? colonnes.get(f.getName()) : null;
            entetes.add(libelle != null ? libelle : f.getName());
        }
        return entetes;
    }
}
