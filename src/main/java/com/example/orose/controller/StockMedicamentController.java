package com.example.orose.controller;

import com.example.orose.dto.EntreeStockMedicamentDTO;
import com.example.orose.service.StockMedicamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/stock/medicament")
public class StockMedicamentController {

    @Autowired
    private StockMedicamentService stockService;

    @GetMapping("/entree")
    public String afficherFormulaireEntree(Model model) {
        model.addAttribute("entrees", stockService.getEntreesStockMedicament());
        return "stock/entree";
    }

    @PostMapping("/entree")
    public String enregistrerEntree(@ModelAttribute EntreeStockMedicamentDTO dto, RedirectAttributes redirectAttributes) {
        stockService.enregistrerEntreeMedicament(dto);
        redirectAttributes.addFlashAttribute("message", "Entrée de médicament enregistrée.");
        return "redirect:/stock/medicament/entree";
    }

    @GetMapping("/perte")
    public String afficherFormulairePerte(Model model) {
        return "stock/sortie";
    }

    @PostMapping("/perte")
    public String enregistrerPerte(
            @RequestParam Integer idEntreeStock,
            @RequestParam BigDecimal quantite,
            @RequestParam String motif,
            @RequestParam Integer idUtilisateur,
            RedirectAttributes redirectAttributes) {
        
        stockService.enregistrerPerteMedicament(idEntreeStock, quantite, motif, idUtilisateur);
        redirectAttributes.addFlashAttribute("message", "Perte enregistrée avec succès.");
        return "redirect:/stock/medicament/perte";
    }
}