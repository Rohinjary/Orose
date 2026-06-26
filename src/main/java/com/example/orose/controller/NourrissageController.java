package com.example.orose.controller;

import com.example.orose.dto.nourrissage.DashboardDTO;
import com.example.orose.service.BassinService;
import com.example.orose.service.nourrissage.DashboardService;
import com.example.orose.service.nourrissage.NourrissageService;
import com.example.orose.service.nourrissage.PlanningService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nourrissage")
public class NourrissageController {

    private final NourrissageService nourrissageService;
    private final BassinService bassinService;
    private final PlanningService planningService;
    private final DashboardService dashboardService;

    public NourrissageController(NourrissageService nourrissageService, BassinService bassinService,
            PlanningService planningService, DashboardService dashboardService) {
        this.nourrissageService = nourrissageService;
        this.bassinService = bassinService;
        this.planningService = planningService;
        this.dashboardService = dashboardService;
    }

    private void preparerLayoutNourissage(Model model, String breadcrumbCurrent, String currentPage) {
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("currentGroup", "nourrissage");
        model.addAttribute("breadcrumbParent", "Nourrissage");
        model.addAttribute("breadcrumbCurrent", breadcrumbCurrent);
    }

    @PostMapping("/valider/{id}")
    public String validerRepasDirect(@PathVariable("id") Integer idDistribution,
            RedirectAttributes redirectAttributes) {

        try {

            nourrissageService.valider(idDistribution, 1);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Nourrissage validé avec succès.");

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            System.out.println("Erreur lors de la validation du nourrissage controller: " + e.getMessage());
        }
        return "redirect:/nourrissage/planning"; // Redirige vers la page du planning du jour
    }

    @PostMapping("/enregistrer")
    public String enregistrer(
            @RequestParam String codeBassin,
            @RequestParam Integer idAliment,
            @RequestParam Double quantiteKg,
            @RequestParam String dateDistribution,
            @RequestParam LocalTime heure,
            RedirectAttributes redirectAttributes) {

        try {
            nourrissageService.enregistrer(
                    codeBassin,
                    idAliment,
                    BigDecimal.valueOf(quantiteKg),
                    1,
                    LocalDate.parse(dateDistribution),
                    heure);

            redirectAttributes.addFlashAttribute("success", "Distribution enregistrée !");
        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/nourrissage/distribution";
        }

        return "redirect:/nourrissage/planning";
    }

    @GetMapping("/distribution")
    public String distribution(Model model) {
        preparerLayoutNourissage(model, "Distribution", "distribution");

        model.addAttribute("bassins", bassinService.getBassinsActifsEtEnTraitement());
        model.addAttribute("aliments", nourrissageService.getAlimentsDisponibles());
        return "nourrissage/saisie_nourri";
    }

    @GetMapping("/planning")
    public String planning(Model model) {
        preparerLayoutNourissage(model, "Planning", "planning");

        Integer idUtilisateur = 1;

        model.addAttribute(
                "bassins",
                planningService.construireTableau(idUtilisateur));

        DashboardDTO dashboard = dashboardService.chargerDashboard(idUtilisateur);

        model.addAttribute("dashboard", dashboard);

        return "nourrissage/dash_nourri";
    }

}