package com.example.orose.controller;

import com.example.orose.dto.IncidentDTO;
import com.example.orose.dto.TraitementDTO;
import com.example.orose.service.IncidentService;
import com.example.orose.service.SanitaireService;
import com.example.orose.service.TraitementService;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.repository.EntreeStockMedicamentRepository;
import com.example.orose.repository.TraitementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

@Controller
@RequestMapping("/sanitaire")
public class SanitaireController {

    @Autowired
    private IncidentService incidentService;
    @Autowired
    private TraitementService traitementService;
    @Autowired
    private SanitaireService sanitaireService;
    @Autowired
    private TraitementRepository traitementRepository;

    @Autowired
    private CycleBassinAssocRepository cycleBassinAssocRepository;
    @Autowired
    private BassinRepository bassinRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private EntreeStockMedicamentRepository entreeStockMedicamentRepository;

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("incidents",
                sanitaireService.getHistoriqueSanitaire(null, null, null, null, null, PageRequest.of(0, 100))
                        .getContent());
        model.addAttribute("currentGroup", "sanitaire");
        model.addAttribute("currentPage", "index");
        model.addAttribute("breadcrumbParent", "Module Sanitaire");
        model.addAttribute("breadcrumbCurrent", "Registre des Incidents");
        return "Sanitaire/index";
    }

    @GetMapping("/declaration")
    public String afficherFormulaireDeclaration(Model model) {
        model.addAttribute("incidentDTO", new IncidentDTO());
        model.addAttribute("cycleBassinAssocs", cycleBassinAssocRepository.findByEstClotureFalse().stream()
                .filter(assoc -> assoc.getBassin() != null && assoc.getBassin().getStatutActuel() != null)
                .filter(assoc -> {
                    String code = assoc.getBassin().getStatutActuel().getCode();
                    return "ACTIF".equalsIgnoreCase(code) || "EN_TRAITEMENT".equalsIgnoreCase(code);
                })
                .toList());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        model.addAttribute("currentGroup", "sanitaire");
        model.addAttribute("currentPage", "declaration");
        model.addAttribute("breadcrumbParent", "Module Sanitaire");
        model.addAttribute("breadcrumbCurrent", "Déclaration d'incident");
        return "Sanitaire/declaration";
    }

    @PostMapping("/declaration")
    public String declarerIncident(@ModelAttribute IncidentDTO dto, RedirectAttributes redirectAttributes) {
        try {
            incidentService.declarerIncident(dto);
            redirectAttributes.addFlashAttribute("message", "Incident déclaré avec succès.");
            return "redirect:/sanitaire/index";
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/sanitaire/declaration";
        }
    }

    @GetMapping("/traitement")
    public String afficherFormulaireTraitement(@RequestParam("incidentId") Integer incidentId, Model model) {
        TraitementDTO traitementDTO = new TraitementDTO();
        traitementDTO.setIdIncident(incidentId);
        model.addAttribute("traitementDTO", traitementDTO);
        model.addAttribute("medicaments",
                entreeStockMedicamentRepository.findByQuantiteRestanteGreaterThan(BigDecimal.ZERO));
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        model.addAttribute("currentGroup", "sanitaire");
        model.addAttribute("currentPage", "traitement");
        model.addAttribute("breadcrumbParent", "Module Sanitaire");
        model.addAttribute("breadcrumbCurrent", "Saisie de Traitement");
        return "Sanitaire/traitement";
    }

    @PostMapping("/traitement")
    public String enregistrerTraitement(@ModelAttribute TraitementDTO dto, RedirectAttributes redirectAttributes) {
        traitementService.enregistrerTraitement(dto);
        redirectAttributes.addFlashAttribute("message", "Traitement enregistré avec succès.");
        return "redirect:/sanitaire/historique";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", sanitaireService.getDashboardSanitaire());
        model.addAttribute("currentGroup", "sanitaire");
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("breadcrumbParent", "Module Sanitaire");
        model.addAttribute("breadcrumbCurrent", "Tableau de bord");
        return "Sanitaire/dashboard";
    }

    @GetMapping("/historique")
    public String historique(@RequestParam(required = false) Integer idBassin,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String debut,
            @RequestParam(required = false) String fin,
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        LocalDateTime dateDebut = null;
        LocalDateTime dateFin = null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if (debut != null && !debut.isEmpty()) {
            dateDebut = LocalDateTime.parse(debut + "T00:00:00", formatter);
        }
        if (fin != null && !fin.isEmpty()) {
            dateFin = LocalDateTime.parse(fin + "T23:59:59", formatter);
        }

        model.addAttribute("historiquePage",
                sanitaireService.getHistoriqueSanitaire(idBassin, type, dateDebut, dateFin, statut,
                        PageRequest.of(page, size)));
        model.addAttribute("stats", sanitaireService.getHistoriqueStats());
        model.addAttribute("bassins", bassinRepository.findAll());
        model.addAttribute("traitementsParIncident", traitementRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.getIncident().getId())));
        model.addAttribute("filtreBassin", idBassin);
        model.addAttribute("filtreType", type);
        model.addAttribute("filtreDebut", debut);
        model.addAttribute("filtreFin", fin);
        model.addAttribute("filtreStatut", statut);
        model.addAttribute("currentGroup", "sanitaire");
        model.addAttribute("currentPage", "historique");
        model.addAttribute("breadcrumbParent", "Module Sanitaire");
        model.addAttribute("breadcrumbCurrent", "Historique");
        return "Sanitaire/historique";
    }

    @PostMapping("/resoudre/{id}")
    public String resoudreIncident(@PathVariable Integer id) {
        incidentService.resoudreIncident(id);
        return "redirect:/sanitaire/historique";
    }
}
