package com.example.orose.controller;

import com.example.orose.common.io.ImportService;
import com.example.orose.dto.PeseeDTO;
import com.example.orose.dto.DeclarRecolteFormDTO;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.SuiviHebdoBassin;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.service.AlerteService;
import com.example.orose.service.BiologiqueService;
import com.example.orose.service.BiologiqueRecolteService;
import com.example.orose.service.PeseeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/biologique")
public class BiologiqueController {

    private final BiologiqueService biologiqueService;
    private final BiologiqueRecolteService recolteService;
    private final PeseeService peseeService;
    private final AlerteService alerteService;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final UtilisateurRepository utilisateurRepository;

    public BiologiqueController(BiologiqueService biologiqueService,
                                BiologiqueRecolteService recolteService,
                                PeseeService peseeService,
                                AlerteService alerteService,
                                CycleBassinAssocRepository cycleBassinAssocRepository,
                                UtilisateurRepository utilisateurRepository) {
        this.biologiqueService = biologiqueService;
        this.recolteService = recolteService;
        this.peseeService = peseeService;
        this.alerteService = alerteService;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    private void preparerLayoutBiologique(Model model, String breadcrumbCurrent, String currentPage) {
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("currentGroup", "biologique");
        model.addAttribute("breadcrumbParent", "Suivi biologique");
        model.addAttribute("breadcrumbCurrent", breadcrumbCurrent);
    }

    @GetMapping
    public String liste(Model model) {
        preparerLayoutBiologique(model, "Ta bleau de bord", "tableau-de-bord");
        model.addAttribute("bassinsEnSuivi", biologiqueService.getBassinsSuivi());
        return "biologique/liste";
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    @Transactional
    public String importerPesees(@RequestParam("fichier") MultipartFile fichier, RedirectAttributes ra) {
        try {
            List<PeseeDTO> dtos = ImportService.importerFichier(PeseeDTO.class, fichier);
            // Trie par bassin puis par date pour que chaque pesee voie bien la
            // precedente lors de la validation sequentielle du service.
            dtos.sort(Comparator.comparing(PeseeDTO::getIdCycleBassinAssoc)
                    .thenComparing(PeseeDTO::getDateSuivi));
            for (PeseeDTO dto : dtos) {
                peseeService.enregistrerPesee(dto);
            }
            ra.addFlashAttribute("succes", dtos.size() + " pesée(s) importée(s) avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", "Import échoué : " + e.getMessage());
        }
        return "redirect:/biologique";
    }

    @GetMapping("/alertes")
    public String alertes(Model model) {
        preparerLayoutBiologique(model, "Alertes biologiques", "alertes-biologiques");
        model.addAttribute("alertes", alerteService.getAlertesBiologiques());
        return "biologique/alertes";
    }

    @GetMapping("/taux-survie")
    public String tauxSurvie(Model model) {
        preparerLayoutBiologique(model, "Taux de survie", "taux-survie");
        model.addAttribute("statistiquesSurvie", biologiqueService.getStatistiquesSurvieActifs());
        model.addAttribute("tauxSurvieGeneral", biologiqueService.getTauxSurvieGeneral());
        return "biologique/taux-survie";
    }

    @PostMapping("/alertes/{id}/resoudre")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String resoudreAlerte(@PathVariable Long id, RedirectAttributes ra) {
        Long idUtilisateur = 1L; // TODO: session
        try {
            alerteService.resoudreAlerte(id, idUtilisateur);
            ra.addFlashAttribute("succes", "Alerte résolue");
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/biologique/alertes";
    }

    // ── Nouvelle pesée ────────────────────────────────────────────────────────

    @GetMapping("/{id}/pesee/nouvelle")
    public String formulairePesee(@PathVariable("id") Integer id, Model model) {
        CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(id.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Association cycle-bassin introuvable"));

        // Date de la dernière pesée : bloque toute date antérieure ou égale
        LocalDate dateDernierePesee = peseeService.getDernierePesee(id)
                .map(SuiviHebdoBassin::getDateSuivi)
                .orElse(null);

        preparerLayoutBiologique(model, "Nouvelle pesée", "nouvelle-pesee");
        model.addAttribute("peseeDTO", new PeseeDTO());
        model.addAttribute("idCycleBassinAssoc", id);
        model.addAttribute("assoc", assoc);
        model.addAttribute("modeEdition", false);
        model.addAttribute("dateDernierePesee", dateDernierePesee);
        model.addAttribute("techniciens", utilisateurRepository.findAll().stream()
                .filter(u -> "ACTIF".equals(u.getStatut())).toList());
        return "biologique/pesee-form";
    }

    @PostMapping("/{id}/pesee/nouvelle")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String enregistrerPesee(@PathVariable("id") Integer id,
                                   @ModelAttribute PeseeDTO dto,
                                   RedirectAttributes ra) {
        dto.setIdCycleBassinAssoc(id);
        try {
            peseeService.enregistrerPesee(dto);
            ra.addFlashAttribute("succes", "Pesée enregistrée avec succès");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/biologique/" + id + "/pesee/nouvelle";
        }
        return "redirect:/biologique/" + id + "/detail";
    }

    // ── Modifier pesée ────────────────────────────────────────────────────────

    @GetMapping("/pesee/{idPesee}/modifier")
    public String formulaireModifierPesee(@PathVariable Long idPesee, Model model) {
        SuiviHebdoBassin pesee = peseeService.getPeseeById(idPesee);
        CycleBassinAssoc assoc = pesee.getCycleBassinAssoc();

        // En mode édition : la borne min est la pesée qui précède immédiatement
        // celle qu'on modifie (pas la pesée courante elle-même)
        List<SuiviHebdoBassin> toutesLesPesees =
                peseeService.getPeseesByCycleBassinAssoc(assoc.getId());

        LocalDate dateDernierePeseeAvant = toutesLesPesees.stream()
                .filter(p -> !p.getId().equals(idPesee))
                .map(SuiviHebdoBassin::getDateSuivi)
                .filter(d -> d.isBefore(pesee.getDateSuivi()))
                .max(Comparator.naturalOrder())
                .orElse(null);

        PeseeDTO dto = new PeseeDTO();
        dto.setIdCycleBassinAssoc(assoc.getId());
        dto.setDateSuivi(pesee.getDateSuivi());
        dto.setPoidsMoyenGramme(pesee.getPoidsMoyenGramme());
        dto.setTailleMoyenneMm(pesee.getTailleMoyenneMm());
        dto.setIdTechnicien(pesee.getTechnicien().getId().longValue());
        dto.setNotes(pesee.getNotes());

        preparerLayoutBiologique(model, "Modifier pesée", "modifier-pesee");
        model.addAttribute("peseeDTO", dto);
        model.addAttribute("idPesee", idPesee);
        model.addAttribute("idCycleBassinAssoc", assoc.getId());
        model.addAttribute("assoc", assoc);
        model.addAttribute("modeEdition", true);
        model.addAttribute("dateDernierePesee", dateDernierePeseeAvant);
        model.addAttribute("techniciens", utilisateurRepository.findAll().stream()
                .filter(u -> "ACTIF".equals(u.getStatut())).toList());
        return "biologique/pesee-form";
    }

    @PostMapping("/pesee/{idPesee}/modifier")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String modifierPesee(@PathVariable Long idPesee,
                                @ModelAttribute PeseeDTO dto,
                                RedirectAttributes ra) {
        SuiviHebdoBassin pesee = peseeService.getPeseeById(idPesee);
        Integer idCba = pesee.getCycleBassinAssoc().getId();
        try {
            peseeService.modifierPesee(idPesee, dto);
            ra.addFlashAttribute("succes", "Pesée modifiée avec succès");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/biologique/pesee/" + idPesee + "/modifier";
        }
        return "redirect:/biologique/" + idCba + "/detail";
    }

    // ── Archiver pesée ────────────────────────────────────────────────────────

    @PostMapping("/pesee/{idPesee}/archiver")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String archiverPesee(@PathVariable Long idPesee, RedirectAttributes ra) {
        SuiviHebdoBassin pesee = peseeService.getPeseeById(idPesee);
        Integer idCba = pesee.getCycleBassinAssoc().getId();
        try {
            peseeService.archiverPesee(idPesee);
            ra.addFlashAttribute("succes", "Pesée archivée");
        } catch (UnsupportedOperationException | EntityNotFoundException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/biologique/" + idCba + "/detail";
    }

    // ── RÉCOLTE : Formulaire de déclaration ────────────────────────────────────

    @GetMapping("/{id}/recolte/declarer")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String formulaireDeclareRecolte(@PathVariable("id") Integer id, Model model) {
        try {
            DeclarRecolteFormDTO formulaire = recolteService.prepareFormulairDeclaration(id);
            preparerLayoutBiologique(model, "Déclaration de récolte", "declare-recolte");
            model.addAttribute("formulaire", formulaire);
            return "biologique/declare-recolte";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{id}/recolte/valider")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String validerDeclarationRecolte(@PathVariable("id") Integer id,
                                            @RequestParam BigDecimal recolteReelleKg,
                                            Authentication authentication,
                                            RedirectAttributes ra) {
        try {
            // TODO: Récupérer l'utilisateur depuis la session/authentification
            Utilisateur responsable = utilisateurRepository.findById(1L)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

            recolteService.validerDeclarationRecolte(id, recolteReelleKg, responsable);

            ra.addFlashAttribute("succes", "Récolte validée avec succès. " +
                    "La quantité a été enregistrée dans le stock de crevettes.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/biologique/" + id + "/recolte/declarer";
        }
        return "redirect:/biologique";
    }

    // ── Déclaration de récolte (ancien endpoint - peut être supprimé) ────────

    @PostMapping("/{id}/recolte")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String declarerRecolte(@PathVariable("id") Integer id,
                                  @RequestParam String motif,
                                  RedirectAttributes ra) {
        Long idUtilisateur = 1L; // TODO: session
        try {
            biologiqueService.declarerRecolte(id, motif, idUtilisateur);
            ra.addFlashAttribute("succes", "Récolte déclarée : le bassin est passé au statut RECOLTE");
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/biologique/" + id + "/detail";
    }

    // ── Détail ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/recolter")
    @PreAuthorize("hasAnyRole('ADMIN','TECH','RS')")
    public String recolter(@PathVariable("id") Integer id, RedirectAttributes ra) {
        Long idUtilisateur = 1L; // TODO: session
        try {
            biologiqueService.recolterSiCalibreAtteint(id, idUtilisateur);
            ra.addFlashAttribute("succes", "Bassin passe en statut RECOLTE avec succes");
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/biologique/" + id + "/detail";
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable("id") Integer id, Model model) {
        preparerLayoutBiologique(model, "Détail du bassin", "detail-bassin");
        model.addAttribute("detail", biologiqueService.getDetailBiologique(id));
        return "biologique/detail";
    }
}
