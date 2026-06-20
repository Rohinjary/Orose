package com.example.orose.controller;

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
}