package com.example.orose.controller;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.orose.common.io.ExportService;
import com.example.orose.model.IncidentSanitaire;
import com.example.orose.service.SanitaireService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Export CSV / Excel / PDF des listes du module Sanitaire.
 *
 * Routes :
 *  GET /sanitaire/historique/export?format=...&idBassin=&type=&debut=&fin=&statut=&q=
 *  GET /sanitaire/index/export?format=...&q=
 */
@Controller
@RequestMapping("/sanitaire")
public class SanitaireExportController {

    private final SanitaireService sanitaireService;

    public SanitaireExportController(SanitaireService sanitaireService) {
        this.sanitaireService = sanitaireService;
    }

    @GetMapping("/historique/export")
    public void exportHistorique(@RequestParam(defaultValue = "excel") String format,
                                  @RequestParam(required = false) Integer idBassin,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String debut,
                                  @RequestParam(required = false) String fin,
                                  @RequestParam(required = false) String statut,
                                  @RequestParam(required = false) String q,
                                  HttpServletResponse response) throws Exception {
        LocalDateTime dDebut = (debut != null && !debut.isEmpty())
                ? LocalDate.parse(debut).atStartOfDay() : null;
        LocalDateTime dFin = (fin != null && !fin.isEmpty())
                ? LocalDate.parse(fin).atTime(23, 59, 59) : null;

        List<IncidentSanitaire> source = sanitaireService
                .getHistoriqueSanitaire(idBassin, type, dDebut, dFin, statut, PageRequest.of(0, 100_000))
                .getContent();

        List<IncidentRow> data = filtrerParTexte(mapper(source), q);
        ecrire(response, data, "historique-sanitaire", format, "Historique sanitaire", colonnes());
    }

    @GetMapping("/index/export")
    public void exportIncidents(@RequestParam(defaultValue = "excel") String format,
                                 @RequestParam(required = false) String q,
                                 HttpServletResponse response) throws Exception {
        List<IncidentSanitaire> source = sanitaireService
                .getHistoriqueSanitaire(null, null, null, null, null, PageRequest.of(0, 100_000))
                .getContent();

        List<IncidentRow> data = filtrerParTexte(mapper(source), q);
        ecrire(response, data, "registre-incidents", format, "Registre des incidents sanitaires", colonnes());
    }

    private List<IncidentRow> filtrerParTexte(List<IncidentRow> data, String q) {
        if (q == null || q.isBlank()) return data;
        String needle = q.toLowerCase();
        List<IncidentRow> filtre = new ArrayList<>();
        for (IncidentRow r : data) {
            if (r.getBassin().toLowerCase().contains(needle)
                    || r.getTypeIncident().toLowerCase().contains(needle)
                    || r.getNiveauGravite().toLowerCase().contains(needle)
                    || r.getStatut().toLowerCase().contains(needle)
                    || r.getResponsable().toLowerCase().contains(needle)
                    || (r.getDescription() != null && r.getDescription().toLowerCase().contains(needle))) {
                filtre.add(r);
            }
        }
        return filtre;
    }

    private List<IncidentRow> mapper(List<IncidentSanitaire> source) {
        List<IncidentRow> data = new ArrayList<>();
        for (IncidentSanitaire i : source) {
            String bassin = (i.getCycleBassinAssoc() != null && i.getCycleBassinAssoc().getBassin() != null)
                    ? i.getCycleBassinAssoc().getBassin().getCode() : "";
            String responsable = "";
            if (i.getResponsable() != null) {
                responsable = i.getResponsable().getNom()
                        + (i.getResponsable().getPrenom() != null ? " " + i.getResponsable().getPrenom() : "");
            }
            data.add(new IncidentRow(
                    i.getDateDetection(),
                    bassin,
                    i.getTypeIncident(),
                    i.getNiveauGravite(),
                    i.getDescription(),
                    responsable,
                    Boolean.TRUE.equals(i.getEstResolu()) ? "Résolu" : "En cours"));
        }
        return data;
    }

    private LinkedHashMap<String, String> colonnes() {
        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        cols.put("dateDetection", "Date détection");
        cols.put("bassin", "Bassin");
        cols.put("typeIncident", "Type");
        cols.put("niveauGravite", "Gravité");
        cols.put("description", "Description");
        cols.put("responsable", "Responsable");
        cols.put("statut", "Statut");
        return cols;
    }

    // ─────────────────────── Helpers ───────────────────────

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

    public static class IncidentRow {
        private LocalDate dateDetection;
        private String bassin;
        private String typeIncident;
        private String niveauGravite;
        private String description;
        private String responsable;
        private String statut;

        public IncidentRow(LocalDate d, String b, String t, String g, String desc, String r, String s) {
            this.dateDetection = d; this.bassin = b; this.typeIncident = t;
            this.niveauGravite = g; this.description = desc;
            this.responsable = r; this.statut = s;
        }
        public LocalDate getDateDetection() { return dateDetection; }
        public String getBassin() { return bassin; }
        public String getTypeIncident() { return typeIncident; }
        public String getNiveauGravite() { return niveauGravite; }
        public String getDescription() { return description; }
        public String getResponsable() { return responsable; }
        public String getStatut() { return statut; }
    }
}
