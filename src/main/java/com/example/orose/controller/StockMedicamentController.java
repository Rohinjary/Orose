package com.example.orose.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.dto.EntreeStockMedicamentDTO;
import com.example.orose.model.EntreeStockMedicament;
import com.example.orose.repository.MedicamentRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.service.StockMedicamentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/stock/medicament")
public class StockMedicamentController {

    @Autowired
    private StockMedicamentService stockService;
    @Autowired
    private MedicamentRepository medicamentRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/entree")
    public String afficherFormulaireEntree(Model model) {
        // Préparation du DTO pour le formulaire
        model.addAttribute("entreeDto", new EntreeStockMedicamentDTO());
        // Chargement des listes pour les selects
        model.addAttribute("listeMedicaments", medicamentRepository.findAll());
        model.addAttribute("listeResponsables", utilisateurRepository.findAll());
        return "stock/crud_produit_stock";
    }

    @PostMapping("/entree")
    public String enregistrerEntree(@Valid @ModelAttribute("entreeDto") EntreeStockMedicamentDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Vérification des erreurs de validation (annotations dans le DTO)
        if (bindingResult.hasErrors()) {
            model.addAttribute("listeMedicaments", medicamentRepository.findAll());
            model.addAttribute("listeResponsables", utilisateurRepository.findAll());
            return "stock/crud_produit_stock"; // Retourne au formulaire avec les erreurs
        }

        stockService.enregistrerEntreeMedicament(dto);
        redirectAttributes.addFlashAttribute("message", "Entrée de médicament enregistrée avec succès.");
        return "redirect:/stock/medicament/liste";
    }

    @GetMapping("/liste")
    public String afficherListeStock(Model model) {
        // On récupère la liste complète des entrées depuis le service
        List<EntreeStockMedicament> entrees = stockService.getAllEntrees();
        
        // On ajoute cette liste au modèle sous le nom "listeStock" 
        // (Assurez-vous que votre HTML utilise bien ${listeStock} dans la boucle th:each)
        model.addAttribute("listeStock", entrees);
        
        return "stock/list_prod_stock";
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