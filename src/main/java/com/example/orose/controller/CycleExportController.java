package com.example.orose.controller;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.orose.common.io.ExportService;
import com.example.orose.model.Cycle;
import com.example.orose.service.CycleService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Export CSV / Excel / PDF de la liste des cycles.
 *
 * Routes :
 *  GET /cycles/liste/export?format=csv|excel|pdf&q=
 */
@Controller
@RequestMapping("/cycles")
public class CycleExportController {

    private final CycleService cycleService;

    public CycleExportController(CycleService cycleService) {
        this.cycleService = cycleService;
    }

    @GetMapping("/liste/export")
    public void exportListe(@RequestParam(defaultValue = "excel") String format,
                             @RequestParam(required = false) String q,
                             HttpServletResponse response) throws Exception {
        List<CycleRow> data = new ArrayList<>();
        for (Cycle c : cycleService.getCyclesActif()) {
            String espece = "";
            if (c.getEspece() != null) {
                espece = c.getEspece().getNomCourant() != null
                        ? c.getEspece().getNomCourant()
                        : c.getEspece().getNomScientifique();
            }
            String technicien = "";
            if (c.getTechnicien() != null) {
                technicien = c.getTechnicien().getNom()
                        + (c.getTechnicien().getPrenom() != null ? " " + c.getTechnicien().getPrenom() : "");
            }
            data.add(new CycleRow(
                    c.getCodeUniqueCycle(), espece,
                    c.getDateDebut(), c.getDateFinPrevue(),
                    Boolean.TRUE.equals(c.getEstCloture()) ? "Clôturé" : "Actif",
                    technicien));
        }
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            data = data.stream().filter(r ->
                    r.getCode().toLowerCase().contains(needle)
                    || r.getEspece().toLowerCase().contains(needle)
                    || r.getStatut().toLowerCase().contains(needle)
                    || r.getTechnicien().toLowerCase().contains(needle)
            ).collect(java.util.stream.Collectors.toList());
        }
        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        cols.put("code", "Code cycle");
        cols.put("espece", "Espèce");
        cols.put("dateDebut", "Date début");
        cols.put("dateFinPrevue", "Date fin prévue");
        cols.put("statut", "Statut");
        cols.put("technicien", "Technicien");
        ecrire(response, data, "cycles", format, "Cycles de production", cols);
    }

    private <T> void ecrire(HttpServletResponse response, List<T> data, String nomFichier,
                             String format, String titrePdf, LinkedHashMap<String, String> colonnes) throws Exception {
        String fmt = format == null ? "excel" : format.toLowerCase();
        switch (fmt) {
            case "csv":
                response.setContentType("text/csv; charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + nomFichier + ".csv\"");
                ExportService.exportCsv(data, new PrintWriter(response.getWriter()), colonnes);
                break;
            case "pdf":
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + nomFichier + ".pdf\"");
                ExportService.exportPdf(data, response.getOutputStream(), titrePdf, colonnes);
                break;
            case "excel":
            default:
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + nomFichier + ".xlsx\"");
                ExportService.exportExcel(data, response.getOutputStream(), colonnes);
                break;
        }
    }

    public static class CycleRow {
        private String code;
        private String espece;
        private LocalDate dateDebut;
        private LocalDate dateFinPrevue;
        private String statut;
        private String technicien;

        public CycleRow(String c, String e, LocalDate d, LocalDate f, String s, String t) {
            this.code = c; this.espece = e; this.dateDebut = d;
            this.dateFinPrevue = f; this.statut = s; this.technicien = t;
        }
        public String getCode() { return code; }
        public String getEspece() { return espece; }
        public LocalDate getDateDebut() { return dateDebut; }
        public LocalDate getDateFinPrevue() { return dateFinPrevue; }
        public String getStatut() { return statut; }
        public String getTechnicien() { return technicien; }
    }
}
