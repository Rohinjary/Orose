package com.example.orose.controller;

import com.example.orose.dto.EntreeStockAlimentDTO;
import com.example.orose.repository.AlimentRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.service.BassinService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/nourrissage")
public class StockAlimentController {

    private final AlimentRepository alimentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BassinService bassinService;

    public StockAlimentController(AlimentRepository alimentRepository,
            UtilisateurRepository utilisateurRepository,
            BassinService bassinService) {
        this.alimentRepository = alimentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.bassinService = bassinService;
    }

    @GetMapping("/saisie")
    public String afficherFormulaire(Model model) {
        // 1. CRÉATION DE L'OBJET : Ceci est OBLIGATOIRE
        EntreeStockAlimentDTO dto = new EntreeStockAlimentDTO();

        // 2. PASSAGE AU MODÈLE : Le nom "entreeStockAlimentDTO" doit correspondre
        // exactement à celui utilisé dans le th:object de votre fichier HTML.
        model.addAttribute("entreeStockAlimentDTO", dto);

        // 3. DONNÉES POUR LES LISTES
        model.addAttribute("aliments", alimentRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        model.addAttribute("bassins", bassinService.listerBassinsActifs());

        return "nourrissage/saisie_nourri";
    }

    @PostMapping("/stock/enregistrer")
    public String enregistrerDistribution(
            @Valid @ModelAttribute("entreeStockAlimentDTO") EntreeStockAlimentDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // Recharger les listes en cas d'erreur
            model.addAttribute("aliments", alimentRepository.findAll());
            model.addAttribute("utilisateurs", utilisateurRepository.findAll());
            model.addAttribute("bassins", bassinService.listerBassinsActifs());
            return "nourrissage/saisie_nourri";
        }

        redirectAttributes.addFlashAttribute("success", "Distribution enregistrée avec succès !");
        return "redirect:/nourrissage/saisie";
    }
}