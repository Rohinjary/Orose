package com.example.orose.controller;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.orose.common.filter.GenericFilter;
import com.example.orose.common.filter.GenericFilterUtil;
import com.example.orose.common.io.ExportService;
import com.example.orose.dto.StockDetailDTO;
import com.example.orose.dto.nourrissage.JournalDTO;
import com.example.orose.service.StockAlimentService;
import com.example.orose.service.nourrissage.NourrissageService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Export CSV / Excel / PDF des listes du module Nourrissage.
 *
 * Routes :
 *  GET /nourrissage/historique/export?format=csv|excel|pdf&q=
 *  GET /nourrissage/stock-aliment/export?format=csv|excel|pdf&q=
 */
@Controller
@RequestMapping("/nourrissage")
public class NourrissageExportController {

    private final NourrissageService nourrissageService;
    private final StockAlimentService stockAlimentService;

    public NourrissageExportController(NourrissageService nourrissageService,
                                        StockAlimentService stockAlimentService) {
        this.nourrissageService = nourrissageService;
        this.stockAlimentService = stockAlimentService;
    }

    @GetMapping("/historique/export")
    public void exportHistorique(@RequestParam(defaultValue = "excel") String format,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam(required = false) String bassinCode,
                                  @RequestParam(required = false) Long cycleId,
                                  @RequestParam(required = false) Long creneauId,
                                  @RequestParam(required = false) String q,
                                  HttpServletResponse response) throws Exception {
        String codeBassinFiltre = (bassinCode == null || bassinCode.isBlank()) ? null : bassinCode;
        List<JournalDTO> data = nourrissageService.getHistoriqueFiltreDTO(date, codeBassinFiltre, cycleId, creneauId);
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            List<JournalDTO> filtre = new ArrayList<>();
            for (JournalDTO d : data) {
                if ((d.getCodeBassin() != null && d.getCodeBassin().toLowerCase().contains(needle))
                        || (d.getNomAliment() != null && d.getNomAliment().toLowerCase().contains(needle))
                        || (d.getNomResponsable() != null && d.getNomResponsable().toLowerCase().contains(needle))) {
                    filtre.add(d);
                }
            }
            data = filtre;
        }
        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        cols.put("date", "Date");
        cols.put("heure", "Heure");
        cols.put("codeBassin", "Bassin");
        cols.put("nomAliment", "Aliment");
        cols.put("quantite", "Quantité (kg)");
        cols.put("nomResponsable", "Responsable");
        cols.put("statut", "Statut");
        ecrire(response, data, "historique-nourrissage", format, "Historique des distributions de nourriture", cols);
    }

    @GetMapping("/stock-aliment/export")
    public void exportStockAliment(@RequestParam(defaultValue = "excel") String format,
                                    @RequestParam(required = false) String q,
                                    HttpServletResponse response) throws Exception {
        List<StockDetailDTO> data = stockAlimentService.getDetailStocks();
        if (q != null && !q.isBlank()) {
            data = GenericFilterUtil.filtrer(data, new GenericFilter().contains("libelle", q));
        }
        LinkedHashMap<String, String> cols = new LinkedHashMap<>();
        cols.put("libelle", "Désignation");
        cols.put("quantite", "Quantité (kg)");
        cols.put("autonomie", "Autonomie (jours)");
        cols.put("statut", "Statut");
        ecrire(response, data, "stock-aliment", format, "Stock aliments disponibles", cols);
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
}
