package com.example.orose.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller; // Nouveau DTO pour le stock
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.dto.DistributionDTO;
import com.example.orose.dto.EntreeStockDTO;
import com.example.orose.model.Bassin;
import com.example.orose.model.DistributionNourriture;
import com.example.orose.repository.AlimentRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.service.BassinService;
import com.example.orose.service.DashboardService;
import com.example.orose.service.DistributionService;
import com.example.orose.service.StockAlimentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/nourrissage")
public class StockAlimentController {

    private final AlimentRepository alimentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BassinService bassinService;
    private final StockAlimentService stockAlimentService;
    private final DashboardService dashboardService;
    private final DistributionService distributionService;

    public StockAlimentController(AlimentRepository alimentRepository,
            UtilisateurRepository utilisateurRepository,
            BassinService bassinService,
            StockAlimentService stockAlimentService, DashboardService dashboardService,
            DistributionService distributionService) {
        this.alimentRepository = alimentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.bassinService = bassinService;
        this.stockAlimentService = stockAlimentService;
        this.dashboardService = dashboardService;
        this.distributionService = distributionService;
    }

    // --- SECTION : DISTRIBUTION (utilise EntreeStockAlimentDTO) ---

    @GetMapping("/saisie")
    public String afficherFormulaireSaisie(Model model) {
        model.addAttribute("distributionDTO", new DistributionDTO());
        model.addAttribute("aliments", alimentRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAllTechniciens());
        model.addAttribute("bassins", bassinService.listerBassinsActifs());
        return "nourrissage/saisie_nourri";
    }

    @PostMapping("/distribution/enregistrer")
    public String enregistrerDistribution(@Valid @ModelAttribute("distributionDTO") DistributionDTO dto,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // En cas d'erreur, on recharge les listes nécessaires pour le formulaire
            model.addAttribute("aliments", alimentRepository.findAll());
            model.addAttribute("utilisateurs", utilisateurRepository.findAllTechniciens());
            model.addAttribute("bassins", bassinService.listerBassinsActifs());
            // On renvoie vers le formulaire avec l'objet en erreur
            return "nourrissage/saisie_nourri";
        }

        // Appelez votre méthode de service ici avec le nouveau DTO
        // stockAlimentService.enregistrerDistribution(dto);

        redirectAttributes.addFlashAttribute("success", "Distribution enregistrée avec succès !");
        return "redirect:/nourrissage/saisie";
    }

    // --- SECTION : STOCK (utilise EntreeStockDTO) ---

    @GetMapping("/stock/formulaire")
    public String afficherFormulaireStock(Model model) {
        model.addAttribute("entreeStockDTO", new EntreeStockDTO());
        model.addAttribute("aliments", alimentRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAllTechniciens()); // Ajoutez cette ligne
        return "nourrissage/stock_form";
    }

    @PostMapping("/stock/enregistrer")
    public String enregistrerStock(@Valid @ModelAttribute("entreeStockDTO") EntreeStockDTO dto,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            result.getFieldErrors()
                    .forEach(e -> System.out.println("ERREUR : " + e.getField() + " - " + e.getDefaultMessage()));
            model.addAttribute("aliments", alimentRepository.findAll());
            model.addAttribute("utilisateurs", utilisateurRepository.findAllTechniciens());
            return "nourrissage/stock_form";
        }

        try {
            stockAlimentService.enregistrerEntree(dto); // Assurez-vous que le service accepte EntreeStockDTO
            redirectAttributes.addFlashAttribute("success", "Stock enregistré avec succès !");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur : " + e.getMessage());
            model.addAttribute("aliments", alimentRepository.findAll());
            return "nourrissage/stock_form";
        }

        return "redirect:/nourrissage/stock/liste";
    }

    @GetMapping("/stock/liste")
    public String afficherListeStock(Model model) {
        // Vos données existantes
        model.addAttribute("stock_dispo", stockAlimentService.getStockActuelTotal());
        model.addAttribute("valeur_stock", stockAlimentService.getValeurStockTotale());
        model.addAttribute("variationSemaine", stockAlimentService.getVariationStockSemaine());
        model.addAttribute("autonomieJours", stockAlimentService.estimerAutonomieJours());
        model.addAttribute("alertesStock", stockAlimentService.getAlertesStock());

        // AJOUTEZ CETTE LIGNE : On envoie la liste des objets stock au modèle
        // Assurez-vous que stockAlimentService possède une méthode qui retourne une
        // liste
        model.addAttribute("stocks", stockAlimentService.getDetailStocks());

        return "nourrissage/stock_liste";
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
    @GetMapping("/stock/dashboard")
    public String afficherDashboard(Model model) {
        model.addAttribute("stock_dispo", stockAlimentService.getStockActuelTotal());

        // 1. Appel du service qui contient toute ta logique métier
        // Le service retourne la liste des bassins avec leurs statuts calculés
        model.addAttribute("listeBassins", dashboardService.getPlanningDuJour());

        // 2. Retourne le chemin vers ton fichier HTML
        // Il doit être situé dans :
        // src/main/resources/templates/nourrissage/dashboard_nourri.html
        return "nourrissage/dashboard_nourri";
    }

    @PostMapping("/valider-rapide")
    public String validerRapide(@ModelAttribute DistributionDTO dto, RedirectAttributes ra) {
        // 1. Appel du service.
        // Le service doit retourner l'objet 'DistributionNourriture' persisté
        // (sauvegardé)
        // afin que l'on puisse récupérer l'ID généré par la base de données.
        DistributionNourriture dist = distributionService.validerDistribution(dto);

        // 2. Vérification et feedback à l'utilisateur via RedirectAttributes
        if (dist != null && dist.getId() != null) {
            ra.addFlashAttribute("success", "Distribution #" + dist.getId() + " validée avec succès !");
        } else {
            ra.addFlashAttribute("error", "Erreur : La distribution n'a pas pu être enregistrée.");
        }

        // 3. Redirection pour éviter le renvoi du formulaire (Pattern
        // Post-Redirect-Get)
        return "redirect:/nourrissage/stock/dashboard";
    }

    @PostMapping("/valider-planning")
    public String validerPlanning(@ModelAttribute("distributionDTO") DistributionDTO dto, RedirectAttributes ra) {
        // Ici, le service va mettre à jour la ligne existante au lieu d'en créer une
        // nouvelle
        distributionService.validerDistribution(dto);

        ra.addFlashAttribute("success", "Le planning a été validé avec succès.");
        return "redirect:/nourrissage/stock/dashboard";
    }

}