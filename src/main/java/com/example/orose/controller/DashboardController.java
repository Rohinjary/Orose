package com.example.orose.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.orose.service.AccueilService;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.Year;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class DashboardController {

    private final AccueilService accueilService;

    @Autowired
    public DashboardController(AccueilService accueilService) {
        this.accueilService = accueilService;
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
        // model.addAttribute("productionAnnuelle",
        // accueilService.getProductionAnnuelle(targetAnnee));
        // model.addAttribute("productionMensuelle",
        // accueilService.getProductionMensuelle(targetAnnee));
        preparerLayoutDashboard(model, "Accueil", "dashboardP");

        return "dashboard";
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
