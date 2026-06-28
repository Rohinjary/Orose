package com.example.orose.controller.stock;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.orose.common.filter.GenericFilter;
import com.example.orose.common.filter.GenericFilterUtil;
import com.example.orose.common.io.ExportService;
import com.example.orose.dto.stock.MouvementStockDTO;
import com.example.orose.dto.stock.ProduitStockDTO;
import com.example.orose.model.EntreeStockMedicament;
import com.example.orose.model.LotCrevette;
import com.example.orose.repository.EntreeStockMedicamentRepository;
import com.example.orose.service.stock.StockService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Export CSV / Excel / PDF des listes du module Stock.
 *
 * Routes :
 *  GET /stock/produits/export?format=csv|excel|pdf
 *  GET /stock/mouvements/export?format=...&dateMin=YYYY-MM-DD&dateMax=YYYY-MM-DD&q=...
 *  GET /stock/lots-crevettes/export?format=...
 *  GET /stock/medicament/liste/export?format=...
 */
@Controller
@RequestMapping("/stock")
public class StockExportController {

    private final StockService stockService;
    private final EntreeStockMedicamentRepository entreeMedicamentRepository;

    public StockExportController(StockService stockService,
                                 EntreeStockMedicamentRepository entreeMedicamentRepository) {
        this.stockService = stockService;
        this.entreeMedicamentRepository = entreeMedicamentRepository;
    }

    @GetMapping("/produits/export")
    public void exportProduits(@RequestParam(defaultValue = "excel") String format,
                                @RequestParam(required = false) String q,
                                HttpServletResponse response) throws Exception {
        List<ProduitStockDTO> data = stockService.getListeProduits();
        if (q != null && !q.isBlank()) {
            data = GenericFilterUtil.filtrer(data, new GenericFilter().contains("nom", q));
        }
        ecrire(response, data, "produits-stock", format, "Référentiel produits stock");
    }

    @GetMapping("/mouvements/export")
    public void exportMouvements(@RequestParam(defaultValue = "excel") String format,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateMin,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateMax,
                                  @RequestParam(required = false) String q,
                                  HttpServletResponse response) throws Exception {
        List<MouvementStockDTO> data = stockService.getHistoriqueMouvements();
        GenericFilter f = new GenericFilter();
        if (dateMin != null || dateMax != null) f.dateBetween("dateMouvement", dateMin, dateMax);
        if (q != null && !q.isBlank()) f.contains("produit", q);
        data = GenericFilterUtil.filtrer(data, f);
        ecrire(response, data, "historique-mouvements", format, "Historique des mouvements de stock");
    }

    @GetMapping("/lots-crevettes/export")
    public void exportLotsCrevettes(@RequestParam(defaultValue = "excel") String format,
                                     HttpServletResponse response) throws Exception {
        List<LotCrevetteExportRow> data = new ArrayList<>();
        for (LotCrevette l : stockService.getLotsCrevette()) {
            String bassin = (l.getCycleBassinAssoc() != null && l.getCycleBassinAssoc().getBassin() != null)
                    ? l.getCycleBassinAssoc().getBassin().getCode() : "N/A";
            String cycle = (l.getCycleBassinAssoc() != null && l.getCycleBassinAssoc().getCycle() != null)
                    ? l.getCycleBassinAssoc().getCycle().getCodeUniqueCycle() : "N/A";
            data.add(new LotCrevetteExportRow(
                    l.getDateRecolte(),
                    l.getNumeroLotUnique(),
                    l.getBiomasseTotaleKg() != null ? l.getBiomasseTotaleKg().floatValue() : 0f,
                    l.getBiomasseActuelleKg() != null ? l.getBiomasseActuelleKg().floatValue() : 0f,
                    bassin,
                    cycle));
        }
        ecrire(response, data, "lots-crevettes", format, "Lots de crevettes récoltés");
    }

    @GetMapping("/medicament/liste/export")
    public void exportStockMedicament(@RequestParam(defaultValue = "excel") String format,
                                       @RequestParam(required = false) String q,
                                       HttpServletResponse response) throws Exception {
        List<StockMedicamentExportRow> data = new ArrayList<>();
        for (EntreeStockMedicament e : entreeMedicamentRepository.findAll()) {
            data.add(new StockMedicamentExportRow(
                    e.getMedicament() != null ? e.getMedicament().getLibelle() : "",
                    e.getMedicament() != null ? e.getMedicament().getUnite() : "",
                    e.getQuantite() != null ? e.getQuantite().floatValue() : 0f,
                    e.getQuantiteRestante() != null ? e.getQuantiteRestante().floatValue() : 0f,
                    e.getDateReception(),
                    e.getDateExpiration(),
                    e.getResponsable() != null ? e.getResponsable().getNom() : ""));
        }
        if (q != null && !q.isBlank()) {
            data = GenericFilterUtil.filtrer(data, new GenericFilter().contains("medicament", q));
        }
        ecrire(response, data, "stock-medicament", format, "Stock médicament");
    }

    // ─────────────────────── Helpers ───────────────────────

    private <T> void ecrire(HttpServletResponse response, List<T> data, String nomFichier,
                             String format, String titrePdf) throws Exception {
        String fmt = format == null ? "excel" : format.toLowerCase();
        switch (fmt) {
            case "csv":
                response.setContentType("text/csv; charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + nomFichier + ".csv\"");
                ExportService.exportCsv(data, new PrintWriter(response.getWriter()));
                break;
            case "pdf":
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + nomFichier + ".pdf\"");
                ExportService.exportPdf(data, response.getOutputStream(), titrePdf);
                break;
            case "excel":
            default:
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + nomFichier + ".xlsx\"");
                ExportService.exportExcel(data, response.getOutputStream());
                break;
        }
    }

    // ─────────────────────── Rows aplatis pour export ───────────────────────

    public static class LotCrevetteExportRow {
        private LocalDate dateRecolte;
        private String numeroLot;
        private Float biomasseTotaleKg;
        private Float stockActuelKg;
        private String bassin;
        private String cycle;

        public LotCrevetteExportRow(LocalDate d, String n, Float bt, Float sa, String b, String c) {
            this.dateRecolte = d; this.numeroLot = n;
            this.biomasseTotaleKg = bt; this.stockActuelKg = sa;
            this.bassin = b; this.cycle = c;
        }
        public LocalDate getDateRecolte() { return dateRecolte; }
        public String getNumeroLot() { return numeroLot; }
        public Float getBiomasseTotaleKg() { return biomasseTotaleKg; }
        public Float getStockActuelKg() { return stockActuelKg; }
        public String getBassin() { return bassin; }
        public String getCycle() { return cycle; }
    }

    public static class StockMedicamentExportRow {
        private String medicament;
        private String unite;
        private Float quantite;
        private Float quantiteRestante;
        private LocalDate dateReception;
        private LocalDate dateExpiration;
        private String responsable;

        public StockMedicamentExportRow(String m, String u, Float q, Float qr,
                                         LocalDate dr, LocalDate de, String r) {
            this.medicament = m; this.unite = u; this.quantite = q;
            this.quantiteRestante = qr; this.dateReception = dr;
            this.dateExpiration = de; this.responsable = r;
        }
        public String getMedicament() { return medicament; }
        public String getUnite() { return unite; }
        public Float getQuantite() { return quantite; }
        public Float getQuantiteRestante() { return quantiteRestante; }
        public LocalDate getDateReception() { return dateReception; }
        public LocalDate getDateExpiration() { return dateExpiration; }
        public String getResponsable() { return responsable; }

        // dummy override for satisfying any reflection helper that expects setters
        @SuppressWarnings("unused")
        private LocalDateTime ignored;
    }
}
