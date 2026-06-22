package com.example.orose.controller;

import com.example.orose.dto.DistributionDTO;
// IMPORTATIONS CORRIGÉES
import com.example.orose.dto.EntreeStockDTO; // Nouveau DTO pour le stock
import com.example.orose.repository.AlimentRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.service.BassinService;
import com.example.orose.service.StockAlimentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nourrissage")
public class StockAlimentController {

    private final AlimentRepository alimentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BassinService bassinService;
    private final StockAlimentService stockAlimentService;

    public StockAlimentController(AlimentRepository alimentRepository,
            UtilisateurRepository utilisateurRepository,
            BassinService bassinService,
            StockAlimentService stockAlimentService) {
        this.alimentRepository = alimentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.bassinService = bassinService;
        this.stockAlimentService = stockAlimentService;
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

    @GetMapping("/stock/dashbord")
    public String afficherDashbord(Model model) {
        model.addAttribute("stock_dispo", stockAlimentService.getStockActuelTotal());
        return ("nourrissage/dashboard_nourri");
    }

    @GetMapping("/stock/historique")
    public String afficherHistorique(Model model) {
        return ("nourrissage/histo_nourri");
    }
}