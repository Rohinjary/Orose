package com.example.orose.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // Nouveau DTO pour le stock
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.dto.BassinDTO;
import com.example.orose.dto.EntreeStockDTO;
import com.example.orose.dto.nourrissage.JournalDTO;
import com.example.orose.repository.AlimentRepository;
import com.example.orose.repository.CreneauRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.service.BassinService;
import com.example.orose.service.CycleService;
import com.example.orose.service.StockAlimentService;
import com.example.orose.service.nourrissage.NourrissageService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/stock")
public class StockAlimentController {

    private final AlimentRepository alimentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BassinService bassinService;
    private final StockAlimentService stockAlimentService;
    private final CycleService cycleService;
    private final CreneauRepository creneauRepository;
    private final NourrissageService nourrissageService;

    public StockAlimentController(AlimentRepository alimentRepository,
            UtilisateurRepository utilisateurRepository,
            BassinService bassinService,
            StockAlimentService stockAlimentService,
            CycleService cycleService,
            CreneauRepository creneauRepository, NourrissageService nourrissageService) {
        this.alimentRepository = alimentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.bassinService = bassinService;
        this.stockAlimentService = stockAlimentService;
        this.cycleService = cycleService;
        this.creneauRepository = creneauRepository;
        this.nourrissageService = nourrissageService;
    }

    @GetMapping("/formulaire")
    public String afficherFormulaireStock(Model model) {
        model.addAttribute("entreeStockDTO", new EntreeStockDTO());
        model.addAttribute("aliments", alimentRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAllTechniciens()); // Ajoutez cette ligne
        return "nourrissage/stock_form";
    }

    @GetMapping("/liste/produit")
    public String afficherListeProduit(Model model) {
        return "stock/list_prod_stock";
    }

    public String enregistrer(
            @Valid @ModelAttribute("EntreeStockDTO") EntreeStockDTO dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "entree-stock/form";
        }
        try {
            stockAlimentService.enregistrerEntree(dto);
            redirectAttributes.addFlashAttribute("success", "Stock enregistré avec succès !");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur : " + e.getMessage());
            model.addAttribute("aliments", alimentRepository.findAll());
            model.addAttribute("utilisateurs", utilisateurRepository.findAllTechniciens());
            return "nourrissage/stock_form";
        }

        return "redirect:/stock/liste";
    }

    @GetMapping("/liste")
    public String afficherListeStock(Model model) {
        model.addAttribute("stock_dispo", stockAlimentService.getStockActuelTotal());
        model.addAttribute("valeur_stock", stockAlimentService.getValeurStockTotale());
        model.addAttribute("variationSemaine", stockAlimentService.getVariationStockSemaine());

        model.addAttribute("stocks", stockAlimentService.getDetailStocks());

        return "nourrissage/stock_liste";
    }

    @GetMapping("/historique")
    public String afficherHistorique(Model model) {
        // Appel de la méthode que vous avez ajoutée dans votre Service
        List<JournalDTO> historique = nourrissageService.getJournalActivites();

        // On passe la liste au modèle Thymeleaf
        model.addAttribute("historique", historique);

        return "nourrissage/histo_nourri";
    }
    // @GetMapping("/stock/dashboard")
    // public String afficherDashboard(Model model) {
    // // Indicateurs globaux de stock
    // model.addAttribute("stock_dispo", stockAlimentService.getStockActuelTotal());
    // model.addAttribute("valeur_stock",
    // stockAlimentService.getValeurStockTotale());
    // model.addAttribute("autonomieJours",
    // stockAlimentService.estimerAutonomieJours());
    // model.addAttribute("variationSemaine",
    // stockAlimentService.getVariationStockSemaine());

    // // Alertes pour attirer l'attention (ex: seuil critique)
    // model.addAttribute("alertesStock", stockAlimentService.getAlertesStock());

    // // Historique récent (pour visualiser les derniers mouvements)
    // // model.addAttribute("derniersMouvements",
    // // stockAlimentService.getDerniersMouvements(5));

    // return "nourrissage/dashboard_nourri";
    // }
    // @GetMapping("/stock/dashboard")
    // public String afficherDashboard(Model model) {
    // model.addAttribute("stock_dispo", stockAlimentService.getStockActuelTotal());

    // // 1. Appel du service qui contient toute ta logique métier
    // // Le service retourne la liste des bassins avec leurs statuts calculés
    // model.addAttribute("listeBassins", dashboardService.getPlanningDuJour());

    // // 2. Retourne le chemin vers ton fichier HTML
    // // Il doit être situé dans :
    // // src/main/resources/templates/nourrissage/dashboard_nourri.html
    // return "nourrissage/dashboard_nourri";
    // }

}