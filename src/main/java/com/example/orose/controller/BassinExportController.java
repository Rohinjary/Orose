package com.example.orose.controller;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.common.filter.GenericFilter;
import com.example.orose.common.filter.GenericFilterUtil;
import com.example.orose.common.io.ExportService;
import com.example.orose.common.io.ImportService;
import com.example.orose.dto.BassinDTO;
import com.example.orose.dto.BassinSuiviDTO;
import com.example.orose.model.Bassin;
import com.example.orose.model.HistoStatutBassin;
import com.example.orose.service.BassinService;
import com.example.orose.service.BiologiqueService;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Export CSV / Excel / PDF des listes du module Bassin.
 *
 * Routes :
 *  GET /bassins/liste/export?format=csv|excel|pdf&q=
 *  GET /bassins/historique/export?format=...&debut=...&fin=...&typeEtat=...&q=
 */
@Controller
@RequestMapping("/bassins")
public class BassinExportController {

    private static final DateTimeFormatter DATE_EXPORT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BassinService bassinService;
    private final BiologiqueService biologiqueService;

    public BassinExportController(BassinService bassinService, BiologiqueService biologiqueService) {
        this.bassinService = bassinService;
        this.biologiqueService = biologiqueService;
    }

    @GetMapping("/liste/export")
    public void exportListe(@RequestParam(defaultValue = "excel") String format,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) String etat,
                             @RequestParam(required = false) String disponibilite,
                             @RequestParam(required = false) String cycle,
                             HttpServletResponse response) throws Exception {
        List<BassinRow> data = new ArrayList<>();
        for (Bassin b : bassinService.listerBassins()) {
            if (etat != null && !etat.isBlank()
                    && (b.getStatutActuel() == null || !etat.equalsIgnoreCase(b.getStatutActuel().getCode()))) {
                continue;
            }
            BassinSuiviDTO suivi = biologiqueService.getSuiviActifPourBassin(b.getId().longValue()).orElse(null);
            String dispo = suivi != null ? "OCCUPE" : "LIBRE";
            if (disponibilite != null && !disponibilite.isBlank() && !disponibilite.equalsIgnoreCase(dispo)) {
                continue;
            }
            if (cycle != null && !cycle.isBlank()
                    && (suivi == null || suivi.getCodeUniqueCycle() == null
                        || !suivi.getCodeUniqueCycle().toLowerCase().contains(cycle.toLowerCase()))) {
                continue;
            }
            data.add(new BassinRow(
                    b.getCode(),
                    b.getSurfaceM2(),
                    b.getProfondeurMetre(),
                    b.getStatutActuel() != null ? b.getStatutActuel().getLibelle() : "",
                    b.getCreatedAt(),
                    b.getNotes()));
        }
        if (q != null && !q.isBlank()) {
            data = GenericFilterUtil.filtrer(data, new GenericFilter().contains("code", q));
        }
        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        cols.put("code", "Code bassin");
        cols.put("surfaceM2", "Surface (m²)");
        cols.put("profondeurMetre", "Profondeur (m)");
        cols.put("statutActuel", "Statut actuel");
        cols.put("createdAt", "Date création");
        cols.put("notes", "Notes");
        ecrire(response, data, "bassins", format, "Liste des bassins de production", cols);
    }

    @GetMapping("/historique/export")
    public void exportHistorique(@RequestParam(defaultValue = "excel") String format,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate debut,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate fin,
                                  @RequestParam(required = false) String typeEtat,
                                  @RequestParam(required = false) String q,
                                  HttpServletResponse response) throws Exception {
        LocalDateTime debutDT = debut != null ? debut.atStartOfDay() : null;
        LocalDateTime finDT   = fin   != null ? fin.atTime(23, 59, 59) : null;
        List<HistoStatutBassin> source = bassinService.getHistoriqueGlobal(debutDT, finDT, typeEtat, q);
        List<HistoRow> data = new ArrayList<>();
        for (HistoStatutBassin h : source) {
            String utilisateur = "";
            if (h.getUtilisateur() != null) {
                utilisateur = h.getUtilisateur().getNom()
                        + (h.getUtilisateur().getPrenom() != null ? " " + h.getUtilisateur().getPrenom() : "");
            }
            data.add(new HistoRow(
                    h.getBassin() != null ? h.getBassin().getCode() : "",
                    h.getStatutBassin() != null ? h.getStatutBassin().getLibelle() : "",
                    h.getDateChangement() != null ? h.getDateChangement().format(DATE_EXPORT) : "",
                    utilisateur,
                    h.getMotif()));
        }
        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        cols.put("bassin", "Bassin");
        cols.put("statut", "Statut");
        cols.put("dateChangement", "Date");
        cols.put("utilisateur", "Utilisateur");
        cols.put("motif", "Motif");
        ecrire(response, data, "historique-bassins", format, "Historique des transitions de bassins", cols);
    }

    @PostMapping("/liste/import")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String importer(@RequestParam("fichier") MultipartFile fichier, RedirectAttributes ra) {
        try {
            List<BassinDTO> dtos = ImportService.importerFichier(BassinDTO.class, fichier);
            for (BassinDTO dto : dtos) {
                bassinService.creerBassin(dto);
            }
            ra.addFlashAttribute("succes", dtos.size() + " bassin(s) importé(s) avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", "Import échoué : " + e.getMessage());
        }
        return "redirect:/bassins/liste";
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

    // ─────────────────────── Rows aplatis ───────────────────────

    public static class BassinRow {
        private String code;
        private BigDecimal surfaceM2;
        private BigDecimal profondeurMetre;
        private String statutActuel;
        private LocalDateTime createdAt;
        private String notes;

        public BassinRow(String code, BigDecimal s, BigDecimal p, String st, LocalDateTime c, String n) {
            this.code = code; this.surfaceM2 = s; this.profondeurMetre = p;
            this.statutActuel = st; this.createdAt = c; this.notes = n;
        }
        public String getCode() { return code; }
        public BigDecimal getSurfaceM2() { return surfaceM2; }
        public BigDecimal getProfondeurMetre() { return profondeurMetre; }
        public String getStatutActuel() { return statutActuel; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public String getNotes() { return notes; }
    }

    public static class HistoRow {
        private String bassin;
        private String statut;
        private String dateChangement;
        private String utilisateur;
        private String motif;

        public HistoRow(String b, String s, String d, String u, String m) {
            this.bassin = b; this.statut = s;
            this.dateChangement = d; this.utilisateur = u; this.motif = m;
        }
        public String getBassin() { return bassin; }
        public String getStatut() { return statut; }
        public String getDateChangement() { return dateChangement; }
        public String getUtilisateur() { return utilisateur; }
        public String getMotif() { return motif; }
    }
}
