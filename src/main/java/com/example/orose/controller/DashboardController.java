package com.example.orose.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.orose.service.AccueilService;
import com.example.orose.config.AppConfigParams;
import com.example.orose.repository.ParametreRepository;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.Year;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class DashboardController {

    private final AccueilService accueilService;
    private final ParametreRepository parametreRepository;

    @Autowired
    public DashboardController(AccueilService accueilService, ParametreRepository parametreRepository) {
        this.accueilService = accueilService;
        this.parametreRepository = parametreRepository;
    }

    private void preparerLayoutDashboard(Model model, String breadcrumbCurrent, String currentPage) {
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("currentGroup", "dashboard");
        model.addAttribute("breadcrumbParent", "Dashboard");
        model.addAttribute("breadcrumbCurrent", breadcrumbCurrent);
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "annee", required = false) Integer annee, Model model) {
        int targetAnnee = (annee != null) ? annee : Year.now().getValue();
        model.addAttribute("date", LocalDate.now());
        model.addAttribute("annee", targetAnnee);
        model.addAttribute("bassinsActifs", accueilService.etatsTousBassinsActifs());
        model.addAttribute("situationStock", accueilService.getSituationStock());
        
        java.util.List<com.example.orose.model.Parametre> params = parametreRepository.findAll();
        if (params != null && !params.isEmpty()) {
            for (com.example.orose.model.Parametre p : params) {
                if (p.getValeur() == null) continue;
                switch (p.getLabel()) {
                    case "cycle_par_an": model.addAttribute("cycleParAn", p.getValeur().intValue()); break;
                    case "bassin_par_cycle": model.addAttribute("bassinParCycle", p.getValeur().intValue()); break;
                    case "prix_kg_crevette": model.addAttribute("prixKgCrevette", p.getValeur().floatValue()); break;
                    case "poids_cible_gr": model.addAttribute("poidsCibleGr", p.getValeur().floatValue()); break;
                    case "taille_cible_mm": model.addAttribute("tailleCibleMm", p.getValeur().floatValue()); break;
                    case "pl_initial": model.addAttribute("plInitial", p.getValeur().intValue()); break;
                }
            }
        } else {
            // Sécurité en cas de bdd vide pour éviter le null en html
            model.addAttribute("cycleParAn", 3);
            model.addAttribute("bassinParCycle", 3);
            model.addAttribute("prixKgCrevette", 15.0);
            model.addAttribute("poidsCibleGr", 0.02);
            model.addAttribute("tailleCibleMm", 15.0);
            model.addAttribute("plInitial", 25000);
        }
        
        preparerLayoutDashboard(model, "Accueil", "dashboardP");

        return "dashboard";
    }

    @PostMapping("/api/config")
    public String updateConfig(
        @RequestParam int cycleParAn,
        @RequestParam int bassinParCycle,
        @RequestParam float prixKgCrevette,
        @RequestParam float poidsCibleGr,
        @RequestParam float tailleCibleMm,
        @RequestParam int plInitial
    ) {
        updateDbParam("cycle_par_an", BigDecimal.valueOf(cycleParAn));
        updateDbParam("bassin_par_cycle", BigDecimal.valueOf(bassinParCycle));
        updateDbParam("prix_kg_crevette", BigDecimal.valueOf(prixKgCrevette));
        updateDbParam("poids_cible_gr", BigDecimal.valueOf(poidsCibleGr));
        updateDbParam("taille_cible_mm", BigDecimal.valueOf(tailleCibleMm));
        updateDbParam("pl_initial", BigDecimal.valueOf(plInitial));

        AppConfigParams.cycleParAn = cycleParAn;
        AppConfigParams.bassinParCycle = bassinParCycle;
        AppConfigParams.prixKgCrevette = prixKgCrevette;
        AppConfigParams.poidsCibleGr = poidsCibleGr;
        AppConfigParams.tailleCibleMm = tailleCibleMm;
        AppConfigParams.plInitial = plInitial;

        return "redirect:/"; 
    }

    private void updateDbParam(String label, java.math.BigDecimal valeur) {
        com.example.orose.model.Parametre p = parametreRepository.findByLabel(label);
        if (p != null) {
            p.setValeur(valeur);
            parametreRepository.save(p);
        }
    }

    @GetMapping("/api/production-stats")
    @ResponseBody
    public Map<String, Object> getProductionStats(@RequestParam int annee) {
        Map<String, Object> resultat = new HashMap<>();
        resultat.put("mensuelle", accueilService.getProductionMensuelle(annee));
        resultat.put("annuelle", accueilService.getProductionAnnuelle(annee));
        resultat.put("objectifAnnuel", accueilService.getObjectifAnnuel());
        return resultat;
    }
}
