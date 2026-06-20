package com.example.orose.controller;

import com.example.orose.dto.CycleDemarrageDTO;
import com.example.orose.model.Bassin;
import com.example.orose.repository.EspeceCrevetteRepository;
import com.example.orose.service.BassinService;
import com.example.orose.service.CycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/cycles")
public class CycleController {

    private final CycleService cycleService;
    private final EspeceCrevetteRepository especeCrevetteRepository;
    private final BassinService bassinService;

    @Autowired
    public CycleController(CycleService cycleService, EspeceCrevetteRepository especeCrevetteRepository, BassinService bassinService) {
        this.cycleService = cycleService;
        this.especeCrevetteRepository = especeCrevetteRepository;
        this.bassinService = bassinService;
    }

    @GetMapping
    public String formulaireDemarrage(Model model) {
        // Plus de bassin specifique — on passe tous les bassins VIDE
        model.addAttribute("bassinsVides", bassinService.getBassinsParStatut("VIDE"));
        model.addAttribute("especes", especeCrevetteRepository.findAll());
        return "cycle/form";
    }

    @PostMapping
    public String demarrer(@RequestParam List<Long> idBassins,
                           @ModelAttribute CycleDemarrageDTO cycleDTO,
                           RedirectAttributes redirectAttributes) {
        try {
            cycleService.demarrerCycle(idBassins, cycleDTO);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/cycles/nouveau";
        }
        return "redirect:/bassins";
    }
}
