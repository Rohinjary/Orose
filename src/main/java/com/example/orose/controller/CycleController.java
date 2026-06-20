package com.example.orose.controller;

import com.example.orose.dto.CycleDemarrageDTO;
import com.example.orose.model.Bassin;
import com.example.orose.repository.EspeceCrevetteRepository;
import com.example.orose.service.BassinService;
import com.example.orose.service.CycleBassinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bassins/{idBassin}/cycles")
public class CycleController {

    @Autowired
    private CycleBassinService cycleService;

    @Autowired
    private EspeceCrevetteRepository especeCrevetteRepository;

    @Autowired
    private BassinService bassinService;

    // Affiche le formulaire de démarrage
    @GetMapping("/nouveau")
    public String formulaireDemarrage(@PathVariable Long idBassin, Model model) {
        Bassin bassin = bassinService.getBassinById(idBassin);
        model.addAttribute("bassin", bassin);
        model.addAttribute("especes", especeCrevetteRepository.findAll());
        model.addAttribute("cycleDTO", new CycleDemarrageDTO());
        return "cycle/form";
    }

    @PostMapping
    public String demarrer(@PathVariable Long idBassin,
                            @ModelAttribute CycleDemarrageDTO cycleDTO,
                            RedirectAttributes redirectAttributes) {
        Long idUtilisateur = 1L; // à remplacer par l'utilisateur connecté
        try {
            cycleService.demarrerCycle(idBassin, cycleDTO);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/bassins/" + idBassin + "/cycles/nouveau";
        }
        return "redirect:/bassins/" + idBassin;
    }
}
