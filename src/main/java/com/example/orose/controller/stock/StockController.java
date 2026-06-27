package com.example.orose.controller.stock;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.dto.stock.EntreeStockIntrantDTO;
import com.example.orose.dto.stock.SortieStockIntrantDTO;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.service.stock.StockService;

@Controller
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;
    private final UtilisateurRepository utilisateurRepository;

    public StockController(StockService stockService, UtilisateurRepository utilisateurRepository) {
        this.stockService = stockService;
        this.utilisateurRepository = utilisateurRepository;
    }

    private void preparerLayout(Model model, String breadcrumbCurrent, String currentPage) {
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("currentGroup", "stock");
        model.addAttribute("breadcrumbParent", "Gestion des Stocks");
        model.addAttribute("breadcrumbCurrent", breadcrumbCurrent);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        preparerLayout(model, "Tableau de bord", "stock-dashboard");
        model.addAttribute("dashboard", stockService.getDashboard());
        model.addAttribute("derniersMouvements", stockService.getDerniersMouvements(5));
        return "stock/dashboard";
    }

    @GetMapping("/produits")
    public String listeProduits(Model model) {
        preparerLayout(model, "Liste des produits", "stock-produits");
        model.addAttribute("produits", stockService.getListeProduits());
        model.addAttribute("alertes", stockService.getAlertes());
        return "stock/index";
    }

@GetMapping("/entree")
public String formulaireEntree(Model model) {
    preparerLayout(model, "Entrée stock", "stock-entree");
    EntreeStockIntrantDTO dto = new EntreeStockIntrantDTO();
    dto.setTypeProduit("MEDICAMENT");
    model.addAttribute("entreeDTO", dto);
    model.addAttribute("medicaments", stockService.getMedicaments());
    model.addAttribute("utilisateurs", utilisateurRepository.findAll());
    return "stock/entree";
}

    @PostMapping("/entree")
    public String enregistrerEntree(@ModelAttribute EntreeStockIntrantDTO dto,
                                     RedirectAttributes redirectAttributes) {
        try {
            stockService.enregistrerEntreeIntrant(dto);
            redirectAttributes.addFlashAttribute("success", "Entrée enregistrée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/stock/produits";
    }

    @GetMapping("/sortie")
    public String formulaireSortie(Model model) {
        preparerLayout(model, "Sortie stock", "stock-sortie");
        SortieStockIntrantDTO dto = new SortieStockIntrantDTO();
        dto.setTypeProduit("MEDICAMENT");
        model.addAttribute("sortieDTO", dto);
        model.addAttribute("medicaments", stockService.getMedicaments());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "stock/sortie";
    }

    @PostMapping("/sortie")
    public String enregistrerSortie(@ModelAttribute SortieStockIntrantDTO dto,
                                     RedirectAttributes redirectAttributes) {
        try {
            stockService.enregistrerSortieManuelle(dto);
            redirectAttributes.addFlashAttribute("success", "Sortie enregistrée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/stock/produits";
    }

    @GetMapping("/mouvements")
    public String historiqueMouvements(Model model) {
        preparerLayout(model, "Historique des mouvements", "stock-mouvements");
        model.addAttribute("mouvements", stockService.getHistoriqueMouvements());
        return "stock/historique";
    }

    @GetMapping("/lots-crevettes")
    public String lotsCrevettes(Model model) {
        preparerLayout(model, "Lots crevettes", "stock-lots");
        model.addAttribute("lots", stockService.getLotsCrevette());
        return "stock/lots_crevettes";
    }
}
