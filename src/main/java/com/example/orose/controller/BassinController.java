package com.example.orose.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.dto.BassinDTO;
import com.example.orose.dto.BassinDetailDTO;
import com.example.orose.dto.BassinGrilleDTO;
import com.example.orose.dto.HistoAvecAvantDTO;
import com.example.orose.model.Bassin;
import com.example.orose.service.BassinGrilleService;
import com.example.orose.service.BassinService;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/bassins")
public class BassinController {

    private final BassinService bassinService;
    private final BassinGrilleService bassinGrilleService;

    public BassinController(BassinService bassinService, BassinGrilleService bassinGrilleService) {
        this.bassinService = bassinService;
        this.bassinGrilleService = bassinGrilleService;
    }

    @GetMapping
    public String lister(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String disponibilite,
            @RequestParam(required = false) String cycleActif,
            Model model) {

        List<BassinGrilleDTO> grille = bassinGrilleService.getGrilleBassins();

        if (statut != null && !statut.isBlank()) {
            grille = grille.stream()
                    .filter(b -> statut.equals(b.getStatutCode()))
                    .collect(Collectors.toList());
        }
        if ("disponible".equals(disponibilite)) {
            grille = grille.stream()
                    .filter(b -> b.getCodeUniqueCycle() == null)
                    .collect(Collectors.toList());
        } else if ("occupe".equals(disponibilite)) {
            grille = grille.stream()
                    .filter(b -> b.getCodeUniqueCycle() != null)
                    .collect(Collectors.toList());
        }
        if (cycleActif != null && !cycleActif.isBlank()) {
            String search = cycleActif.toLowerCase();
            grille = grille.stream()
                    .filter(b -> b.getCodeUniqueCycle() != null &&
                                 b.getCodeUniqueCycle().toLowerCase().contains(search))
                    .collect(Collectors.toList());
        }

        model.addAttribute("grille", grille);
        model.addAttribute("statut", statut);
        model.addAttribute("disponibilite", disponibilite);
        model.addAttribute("cycleActif", cycleActif);
        return "bassin/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("bassinDTO", new BassinDTO());
        model.addAttribute("modeEdition", false);
        return "bassin/form";
    }

    @PostMapping
    public String creer(@ModelAttribute BassinDTO bassinDTO, Model model) {
        try {
            bassinService.creerBassin(bassinDTO, 1L);
            return "redirect:/bassins";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("bassinDTO", bassinDTO);
            model.addAttribute("modeEdition", false);
            return "bassin/form";
        }
    }

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

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            bassinService.supprimerBassin(id);

            redirectAttributes.addFlashAttribute(
                "succes",
                "Bassin supprimé avec succès."
            );

            return "redirect:/bassins";

        } catch (IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                "erreur",
                e.getMessage()
            );

            return "redirect:/bassins/" + id;
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BassinDetailDTO detail = bassinGrilleService.getDetailBassin(id);
        List<HistoAvecAvantDTO> historique = bassinService.getHistoriqueAvecAvant(id);
        List<String> transitions = bassinService.getTransitionsAutorisees(id);
        detail.setHistoriqueRecent(historique);
        detail.setTransitionsAutorisees(transitions);
        model.addAttribute("detail", detail);
        return "bassin/detail";
    }

    @PostMapping("/{id}/statut")
    public String changerStatut(@PathVariable Long id,
                                @RequestParam String nouveauStatut,
                                @RequestParam String motif,
                                RedirectAttributes redirectAttributes) {
        Long idUtilisateur = 1L;
        try {
            bassinService.changerStatutBassin(id, nouveauStatut, motif, idUtilisateur);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/bassins/" + id;
    }

    @GetMapping("/historique")
    public String historiqueGlobal(
            @RequestParam(required = false) Long idBassin,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime debut,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fin,
            @RequestParam(required = false) String typeEtat,
            Model model) {

        List<HistoAvecAvantDTO> historique =
                bassinService.getHistoriqueGlobalAvecAvant(idBassin, debut, fin, typeEtat);

        model.addAttribute("historique", historique);
        model.addAttribute("bassins", bassinService.listerBassins());
        model.addAttribute("idBassinSelected", idBassin);
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("typeEtat", typeEtat);
        return "bassin/historique";
    }

    @GetMapping("/historique/export")
    public void exportCsv(
            @RequestParam(required = false) Long idBassin,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime debut,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fin,
            @RequestParam(required = false) String typeEtat,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"historique_bassins.csv\"");

        List<HistoAvecAvantDTO> records =
                bassinService.getHistoriqueGlobalAvecAvant(idBassin, debut, fin, typeEtat);

        PrintWriter writer = response.getWriter();
        writer.println("Bassin concerné,État avant,État après,Date et heure,Utilisateur responsable,Motif");
        for (HistoAvecAvantDTO h : records) {
            writer.printf("%s,%s,%s,%s,%s,%s%n",
                    csv(h.getCodeBassin()),
                    csv(h.getStatutAvantLibelle()),
                    csv(h.getStatutApresLibelle()),
                    h.getDateChangement() != null ? h.getDateChangement().toString() : "",
                    csv(h.getUtilisateurNom()),
                    csv(h.getMotif() != null ? h.getMotif() : ""));
        }
    }

    private String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
