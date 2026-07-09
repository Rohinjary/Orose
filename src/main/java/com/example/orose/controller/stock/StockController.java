package com.example.orose.controller.stock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.dto.stock.EnregistrerPerteCrevetteDTO;
import com.example.orose.dto.stock.EntreeStockAlimentDTO;
import com.example.orose.dto.stock.EntreeStockIntrantDTO;
import com.example.orose.dto.stock.SortieStockIntrantDTO;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.service.stock.StockService;
import org.springframework.web.bind.annotation.RequestParam;

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
        return "stock/dashboard";
    }

    @GetMapping("/produits")
    public String listeProduits(@RequestParam(required = false) String categorie, Model model) {
        String titre = "Stock actuel";
        String page = "stock-produits";
        if (categorie != null) {
            titre += " — " + categorie;
            page = "stock-produits-" + categorie.toLowerCase();
        }
        preparerLayout(model, titre, page);
        model.addAttribute("categorie", categorie);
        model.addAttribute("produits", stockService.getListeProduits(categorie));
        model.addAttribute("alertes", stockService.getAlertes(categorie));
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

    @GetMapping("/entree/aliment")
    public String formulaireEntreeAliment(Model model) {
        preparerLayout(model, "Entrée stock", "stock-entree-aliment");
        EntreeStockAlimentDTO dto = new EntreeStockAlimentDTO();
        model.addAttribute("entreeDTO", dto);
        model.addAttribute("aliments", stockService.getAliments());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "stock/aliment/entree";
    }

    @PostMapping("/entree")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
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

    @PostMapping("/entree/aliment")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String enregistrerEntreeAliment(@ModelAttribute EntreeStockAlimentDTO dto,
            RedirectAttributes redirectAttributes) {

        try {

            stockService.enregistrerEntreeAliment(dto);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Entree aliment validé avec succès.");
            return "redirect:/stock/mouvements?categorie=ALIMENT";
        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            System.out.println("Erreur lors de la validation du entree aliment: " + e.getMessage());
            return "redirect:/stock/entree/aliment";
        }

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
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
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
    public String historiqueMouvements(@RequestParam(required = false) String categorie, Model model) {
        String titre = "Historique des mouvements";
        String page = "stock-mouvements";
        if (categorie != null) {
            titre += " — " + categorie;
            page = "stock-mouvements-" + categorie.toLowerCase();
        }
        preparerLayout(model, titre, page);
        model.addAttribute("categorie", categorie);
        model.addAttribute("mouvements", stockService.getHistoriqueMouvements(categorie));
        return "stock/historique";
    }

    @GetMapping("/lots-crevettes")
    public String lotsCrevettes(Model model) {
        preparerLayout(model, "Lots crevettes", "stock-lots");
        model.addAttribute("lots", stockService.getLotsCrevette());
        return "stock/lots_crevettes";
    }

    @GetMapping("/pertes-crevettes")
    public String pertesCrevettes(Model model) {
        preparerLayout(model, "Registre pertes crevettes", "stock-pertes-crevettes");
        model.addAttribute("pertes", stockService.getPertesCrevettes());
        model.addAttribute("lots", stockService.getLotsCrevette());
        model.addAttribute("perteDTO", new EnregistrerPerteCrevetteDTO());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "stock/perte/crevettes";
    }

    @PostMapping("/pertes-crevettes")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String enregistrerPerteCrevette(@ModelAttribute EnregistrerPerteCrevetteDTO dto,
            @RequestParam Integer id_utilisateur,
            RedirectAttributes redirectAttributes) {
        try {
            stockService.enregistrerPerteCrevette(dto, id_utilisateur);
            redirectAttributes.addFlashAttribute("success", "Perte enregistrée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/stock/pertes-crevettes";
    }
}
