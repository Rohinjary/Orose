package com.example.orose.controller;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.dto.BassinDTO;
import com.example.orose.model.Bassin;
import com.example.orose.service.BassinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bassins")
public class BassinController {

    @Autowired
    private BassinService bassinService;

    // Liste de tous les bassins
    @GetMapping
    public String lister(Model model) {
        model.addAttribute("bassins", bassinService.listerBassins());
        return "bassin/liste";
    }

    // Affiche le formulaire vide de création
    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("bassinDTO", new BassinDTO());
        model.addAttribute("modeEdition", false);
        return "bassin/form";
    }

    // Traite la soumission du formulaire de création
    @PostMapping
    public String creer(@ModelAttribute BassinDTO bassinDTO, Model model) {
        try {
            bassinService.creerBassin(bassinDTO);
            return "redirect:/bassins";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("bassinDTO", bassinDTO);
            model.addAttribute("modeEdition", false);
            return "bassin/form";
        }
    }

    // Affiche le formulaire pré-rempli de modification
    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        Bassin bassin = bassinService.getBassinById(id);

        BassinDTO dto = new BassinDTO();
        dto.setCode(bassin.getCode());
        dto.setSurface_m2(bassin.getSurfaceM2());
        dto.setProfondeur_metre(bassin.getProfondeurMetre());
        dto.setNotes(bassin.getNotes());

        model.addAttribute("bassinDTO", dto);
        model.addAttribute("idBassin", id);
        model.addAttribute("modeEdition", true);
        return "bassin/form";
    }

    // Traite la soumission du formulaire de modification
    @PostMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, @ModelAttribute BassinDTO bassinDTO, Model model) {
        try {
            bassinService.modifierBassin(id, bassinDTO);
            return "redirect:/bassins";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("bassinDTO", bassinDTO);
            model.addAttribute("idBassin", id);
            model.addAttribute("modeEdition", true);
            return "bassin/form";
        }
    }

    // Suppression (un formulaire HTML ne sait faire que GET/POST, donc POST ici)
    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        bassinService.supprimerBassin(id);
        return "redirect:/bassins";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Bassin bassin = bassinService.getBassinById(id);
        List<String> transitions = bassinService.getTransitionsAutorisees(id);

        model.addAttribute("bassin", bassin);
        model.addAttribute("transitions", transitions);
        return "bassin/detail";
    }

    // Traite le changement de statut
    @PostMapping("/{id}/statut")
    public String changerStatut(@PathVariable Long id,
                                @RequestParam String nouveauStatut,
                                @RequestParam String motif,
                                RedirectAttributes redirectAttributes) {
        Long idUtilisateur = 1L; // à remplacer plus tard par l'utilisateur connecté
        try {
            bassinService.changerStatutBassin(id, nouveauStatut, motif, idUtilisateur);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/bassins/" + id;
    }
}