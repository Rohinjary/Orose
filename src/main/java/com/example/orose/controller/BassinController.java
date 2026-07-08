package com.example.orose.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.dto.BassinDTO;
import com.example.orose.dto.BassinSuiviDTO;
import com.example.orose.model.Bassin;
import com.example.orose.model.HistoStatutBassin;
import com.example.orose.service.BassinService;
import com.example.orose.service.BiologiqueService;

@Controller
@RequestMapping("/bassins")
public class BassinController {

    @Autowired
    private BassinService bassinService;

    @Autowired
    private BiologiqueService biologiqueService;

    private void preparerLayoutBassins(Model model, String breadcrumbCurrent, String currentPage) {
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("currentGroup", "bassins");
        model.addAttribute("breadcrumbParent", "Bassins");
        model.addAttribute("breadcrumbCurrent", breadcrumbCurrent);
    }

    @GetMapping("/liste")
    public String lister(Model model) {
        preparerLayoutBassins(model, "Liste des bassins", "liste");
        List<Bassin> bassins = bassinService.listerBassins();
        model.addAttribute("bassins", bassins);

        // Clé Integer (type réel de Bassin.id) pour que le lookup ${suivisParBassin.get(bassin.id)}
        // fonctionne côté template ; HashMap.put accepte en plus les valeurs null
        // (contrairement à Collectors.toMap/Map.merge), nécessaires pour les bassins sans cycle actif.
        Map<Integer, BassinSuiviDTO> suivisParBassin = new HashMap<>();
        for (Bassin b : bassins) {
            suivisParBassin.put(b.getId(), biologiqueService.getSuiviActifPourBassin(b.getId().longValue()).orElse(null));
        }
        model.addAttribute("suivisParBassin", suivisParBassin);
        return "bassin/liste";
    }

    @GetMapping("/nouveau")
    @PreAuthorize("hasRole('ADMIN')")
    public String formulaireCreation(Model model) {
        preparerLayoutBassins(model, "Nouveau bassin", "nouveau");
        model.addAttribute("bassinDTO", new BassinDTO());
        model.addAttribute("modeEdition", false);
        return "bassin/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String creer(@ModelAttribute BassinDTO bassinDTO, Model model) {
        try {
            bassinService.creerBassin(bassinDTO);
            return "redirect:/bassins/liste";
        } catch (Exception e) {
            preparerLayoutBassins(model, "Nouveau bassin", "nouveau");
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("bassinDTO", bassinDTO);
            model.addAttribute("modeEdition", false);
            return "bassin/form";
        }
    }

    @GetMapping("/{id}/modifier")
    @PreAuthorize("hasRole('ADMIN')")
    public String formulaireModification(@PathVariable Long id, Model model) {
        preparerLayoutBassins(model, "Modifier le bassin", "modifier");
        Bassin bassin = bassinService.getBassinById(id);

        BassinDTO dto = new BassinDTO();
        dto.setCode(bassin.getCode());
        dto.setSurface_m2(bassin.getSurfaceM2());
        dto.setProfondeur_metre(bassin.getProfondeurMetre());
        dto.setNotes(bassin.getNotes());
        dto.setDateCreation(bassin.getCreatedAt().toLocalDate());

        model.addAttribute("bassinDTO", dto);
        model.addAttribute("idBassin", id);
        model.addAttribute("modeEdition", true);
        return "bassin/form";
    }

    @PostMapping("/{id}/modifier")
    @PreAuthorize("hasRole('ADMIN')")
    public String modifier(@PathVariable Long id, @ModelAttribute BassinDTO bassinDTO, Model model) {
        try {
            bassinService.modifierBassin(id, bassinDTO);
            return "redirect:/bassins/liste";
        } catch (Exception e) {
            preparerLayoutBassins(model, "Modifier le bassin", "modifier");
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("bassinDTO", bassinDTO);
            model.addAttribute("idBassin", id);
            model.addAttribute("modeEdition", true);
            return "bassin/form";
        }
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bassinService.supprimerBassin(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/bassins/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        preparerLayoutBassins(model, "Détail du bassin", "detail");
        Bassin bassin = bassinService.getBassinById(id);
        List<String> transitions = bassinService.getTransitionsAutorisees(id);
        List<HistoStatutBassin> historique = bassinService.getHistoriqueStatuts(id, null, null, null);

        model.addAttribute("bassin", bassin);
        model.addAttribute("transitions", transitions);
        model.addAttribute("historique", historique);
        model.addAttribute("suiviActif", biologiqueService.getSuiviActifPourBassin(id).orElse(null));
        return "bassin/detail";
    }

    @PostMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public String changerStatut(@PathVariable Long id,
                                @RequestParam String nouveauStatut,
                                @RequestParam String motif,
                                RedirectAttributes redirectAttributes) {
        Long idUtilisateur = 1L;
        try {
            if (!bassinService.getTransitionsAutorisees(id).contains(nouveauStatut)) {
                throw new IllegalStateException("Ce changement de statut n'est pas autorisé manuellement.");
            }
            bassinService.changerStatutBassin(id, nouveauStatut, motif, idUtilisateur);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/bassins/" + id;
    }

    @GetMapping("/historique")
    public String historiqueGlobal(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate debut,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            @RequestParam(required = false) String typeEtat,
            @RequestParam(required = false) String q,
            Model model) {
        preparerLayoutBassins(model, "Historique global", "historique");
        LocalDateTime debutDT = debut != null ? debut.atStartOfDay() : null;
        LocalDateTime finDT   = fin   != null ? fin.atTime(23, 59, 59) : null;
        model.addAttribute("historique", bassinService.getHistoriqueGlobal(debutDT, finDT, typeEtat, q));
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("typeEtat", typeEtat);
        model.addAttribute("q", q);
        return "bassin/historique";
    }

}