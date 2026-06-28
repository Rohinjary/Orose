package com.example.orose.common.io;

import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;

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
 * Les colonnes correspondent aux champs déclarés (hors id) de la classe des objets.
 */
@Service
public class ExportService {

    private static final String SEPARATEUR_CSV = ";";

    public static <T> void exportCsv(List<T> data, Writer writer) throws Exception {
        if (data == null || data.isEmpty()) return;
        Field[] fields = data.get(0).getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            writer.write(fields[i].getName());
            writer.write(i != fields.length - 1 ? SEPARATEUR_CSV : "\n");
        }
        for (T obj : data) {
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                Object value = fields[i].get(obj);
                writer.write(value == null ? "" : value.toString());
                if (i != fields.length - 1) writer.write(SEPARATEUR_CSV);
            }
            writer.write("\n");
        }
        writer.flush();
    }

    public static <T> void exportExcel(List<T> data, OutputStream outputStream) throws Exception {
        if (data == null || data.isEmpty()) return;
        Field[] fields = data.get(0).getClass().getDeclaredFields();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Données");

            Row header = sheet.createRow(0);
            for (int i = 0; i < fields.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(fields[i].getName());
            }
            for (int r = 0; r < data.size(); r++) {
                Row row = sheet.createRow(r + 1);
                T obj = data.get(r);
                for (int c = 0; c < fields.length; c++) {
                    fields[c].setAccessible(true);
                    Object value = fields[c].get(obj);
                    Cell cell = row.createCell(c);
                    cell.setCellValue(value == null ? "" : value.toString());
                }
            }
            for (int i = 0; i < fields.length; i++) sheet.autoSizeColumn(i);
            workbook.write(outputStream);
        }
    }

    public static <T> void exportPdf(List<T> data, OutputStream outputStream, String titre) throws Exception {
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

        Field[] fields = data.get(0).getClass().getDeclaredFields();
        PdfPTable table = new PdfPTable(fields.length);
        table.setWidthPercentage(100);

        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (Field f : fields) {
            PdfPCell cell = new PdfPCell(new Paragraph(f.getName(), fontHeader));
            cell.setBackgroundColor(new java.awt.Color(220, 220, 220));
            table.addCell(cell);
        }

        Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (T obj : data) {
            for (Field f : fields) {
                f.setAccessible(true);
                Object value = f.get(obj);
                table.addCell(new PdfPCell(new Paragraph(value == null ? "" : value.toString(), fontCell)));
            }
        }
        document.add(table);
        document.close();
    }
}
