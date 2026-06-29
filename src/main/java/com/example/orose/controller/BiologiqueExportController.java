package com.example.orose.controller;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.orose.common.filter.GenericFilter;
import com.example.orose.common.filter.GenericFilterUtil;
import com.example.orose.common.io.ExportService;
import com.example.orose.dto.BassinSuiviDTO;
import com.example.orose.model.Alerte;
import com.example.orose.service.AlerteService;
import com.example.orose.service.BiologiqueService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Export CSV / Excel / PDF des listes du module Biologique.
 *
 * Routes :
 *  GET /biologique/export?format=...&statut=&bassin=&cycle=&q=
 *  GET /biologique/alertes/export?format=...&q=
 */
@Controller
@RequestMapping("/biologique")
public class BiologiqueExportController {

    private final BiologiqueService biologiqueService;
    private final AlerteService alerteService;

    public BiologiqueExportController(BiologiqueService biologiqueService, AlerteService alerteService) {
        this.biologiqueService = biologiqueService;
        this.alerteService = alerteService;
    }

    @GetMapping("/export")
    public void exportSuivi(@RequestParam(defaultValue = "excel") String format,
                             @RequestParam(required = false) String statut,
                             @RequestParam(required = false) String bassin,
                             @RequestParam(required = false) String cycle,
                             @RequestParam(required = false) String q,
                             HttpServletResponse response) throws Exception {
        List<BassinSuiviDTO> data = biologiqueService.getBassinsSuivi();

        GenericFilter f = new GenericFilter();
        if (statut != null && !statut.isBlank()) f.eq("statutCroissance", statut);
        if (bassin != null && !bassin.isBlank()) f.eq("codeBassin", bassin);
        if (cycle != null && !cycle.isBlank()) f.eq("codeUniqueCycle", cycle);
        if (q != null && !q.isBlank()) f.contains("codeBassin", q);
        data = GenericFilterUtil.filtrer(data, f);

        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        cols.put("codeBassin", "Bassin");
        cols.put("codeUniqueCycle", "Cycle");
        cols.put("dateDernierePesee", "Dernière pesée");
        cols.put("semaine", "Semaine");
        cols.put("poidsMoyenActuel", "Poids moyen (g)");
        cols.put("tauxSurvie", "Taux survie (%)");
        cols.put("statutCroissance", "Statut croissance");
        ecrire(response, data, "suivi-biologique", format, "Suivi biologique des bassins", cols);
    }

    @GetMapping("/alertes/export")
    public void exportAlertes(@RequestParam(defaultValue = "excel") String format,
                               @RequestParam(required = false) String q,
                               HttpServletResponse response) throws Exception {
        List<AlerteRow> data = new ArrayList<>();
        for (Alerte a : alerteService.getAlertesBiologiques()) {
            String bassin = "";
            String cycle = "";
            if (a.getCycleBassinAssoc() != null) {
                if (a.getCycleBassinAssoc().getBassin() != null) {
                    bassin = a.getCycleBassinAssoc().getBassin().getCode();
                }
                if (a.getCycleBassinAssoc().getCycle() != null) {
                    cycle = a.getCycleBassinAssoc().getCycle().getCodeUniqueCycle();
                }
            }
            data.add(new AlerteRow(
                    a.getDateCreation(), a.getTypeAlerte(), a.getNiveau(),
                    bassin, cycle, a.getMessage(),
                    Boolean.TRUE.equals(a.getEstResolue()) ? "Résolue" : "Active"));
        }
        if (q != null && !q.isBlank()) {
            data = GenericFilterUtil.filtrer(data, new GenericFilter().contains("bassin", q));
        }
        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        cols.put("dateCreation", "Date création");
        cols.put("type", "Type");
        cols.put("niveau", "Niveau");
        cols.put("bassin", "Bassin");
        cols.put("cycle", "Cycle");
        cols.put("message", "Message");
        cols.put("statut", "Statut");
        ecrire(response, data, "alertes-biologiques", format, "Alertes biologiques", cols);
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

    public static class AlerteRow {
        private LocalDateTime dateCreation;
        private String type;
        private String niveau;
        private String bassin;
        private String cycle;
        private String message;
        private String statut;

        public AlerteRow(LocalDateTime d, String t, String n, String b, String c, String m, String s) {
            this.dateCreation = d; this.type = t; this.niveau = n;
            this.bassin = b; this.cycle = c; this.message = m; this.statut = s;
        }
        public LocalDateTime getDateCreation() { return dateCreation; }
        public String getType() { return type; }
        public String getNiveau() { return niveau; }
        public String getBassin() { return bassin; }
        public String getCycle() { return cycle; }
        public String getMessage() { return message; }
        public String getStatut() { return statut; }
    }
}
