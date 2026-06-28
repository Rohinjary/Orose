package com.example.orose.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orose.common.io.ImportService;
import com.example.orose.common.io.exception.ImportException;
import com.example.orose.dto.AlimentImportDTO;
import com.example.orose.dto.EspeceCrevetteImportDTO;
import com.example.orose.dto.MedicamentImportDTO;
import com.example.orose.model.Aliment;
import com.example.orose.model.EspeceCrevette;
import com.example.orose.model.Medicament;
import com.example.orose.repository.AlimentRepository;
import com.example.orose.repository.EspeceCrevetteRepository;
import com.example.orose.repository.MedicamentRepository;

/**
 * Page admin pour gérer (consulter + importer) les référentiels :
 * Aliments, Médicaments, Espèces de crevette.
 *
 * Toutes les actions sont réservées au rôle ADMIN.
 */
@Controller
@RequestMapping("/admin/referentiels")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReferentielController {

    private final AlimentRepository alimentRepository;
    private final MedicamentRepository medicamentRepository;
    private final EspeceCrevetteRepository especeCrevetteRepository;

    public AdminReferentielController(AlimentRepository alimentRepository,
                                       MedicamentRepository medicamentRepository,
                                       EspeceCrevetteRepository especeCrevetteRepository) {
        this.alimentRepository = alimentRepository;
        this.medicamentRepository = medicamentRepository;
        this.especeCrevetteRepository = especeCrevetteRepository;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("aliments", alimentRepository.findAll());
        model.addAttribute("medicaments", medicamentRepository.findAll());
        model.addAttribute("especes", especeCrevetteRepository.findAll());
        model.addAttribute("currentPage", "admin-referentiels");
        model.addAttribute("currentGroup", "admin");
        model.addAttribute("breadcrumbParent", "Administration");
        model.addAttribute("breadcrumbCurrent", "Référentiels");
        return "admin/referentiels";
    }

    // ─────────────────────── Aliments ───────────────────────

    @PostMapping("/aliments/import")
    @Transactional(rollbackFor = Exception.class)
    public String importerAliments(@RequestParam("fichier") MultipartFile fichier,
                                    @RequestParam(defaultValue = "csv") String format,
                                    RedirectAttributes ra) {
        try {
            List<AlimentImportDTO> dtos = parser(AlimentImportDTO.class, fichier, format);
            int n = 0;
            for (AlimentImportDTO dto : dtos) {
                Aliment a = new Aliment();
                a.setLibelle(dto.getLibelle());
                a.setSeuilMinimumKg(dto.getSeuilMinimumKg());
                alimentRepository.save(a);
                n++;
            }
            ra.addFlashAttribute("succes", n + " aliment(s) importé(s)");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", "Import aliments : " + e.getMessage());
        }
        return "redirect:/admin/referentiels";
    }

    // ─────────────────────── Médicaments ───────────────────────

    @PostMapping("/medicaments/import")
    @Transactional(rollbackFor = Exception.class)
    public String importerMedicaments(@RequestParam("fichier") MultipartFile fichier,
                                       @RequestParam(defaultValue = "csv") String format,
                                       RedirectAttributes ra) {
        try {
            List<MedicamentImportDTO> dtos = parser(MedicamentImportDTO.class, fichier, format);
            int n = 0;
            for (MedicamentImportDTO dto : dtos) {
                Medicament m = new Medicament();
                m.setLibelle(dto.getLibelle());
                m.setUnite(dto.getUnite());
                m.setSeuilMinimum(dto.getSeuilMinimum());
                medicamentRepository.save(m);
                n++;
            }
            ra.addFlashAttribute("succes", n + " médicament(s) importé(s)");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", "Import médicaments : " + e.getMessage());
        }
        return "redirect:/admin/referentiels";
    }

    // ─────────────────────── Espèces ───────────────────────

    @PostMapping("/especes/import")
    @Transactional(rollbackFor = Exception.class)
    public String importerEspeces(@RequestParam("fichier") MultipartFile fichier,
                                   @RequestParam(defaultValue = "csv") String format,
                                   RedirectAttributes ra) {
        try {
            List<EspeceCrevetteImportDTO> dtos = parser(EspeceCrevetteImportDTO.class, fichier, format);
            int n = 0;
            for (EspeceCrevetteImportDTO dto : dtos) {
                EspeceCrevette e = new EspeceCrevette();
                e.setNomScientifique(dto.getNomScientifique());
                e.setNomCourant(dto.getNomCourant());
                especeCrevetteRepository.save(e);
                n++;
            }
            ra.addFlashAttribute("succes", n + " espèce(s) importée(s)");
        } catch (Exception ex) {
            ra.addFlashAttribute("erreur", "Import espèces : " + ex.getMessage());
        }
        return "redirect:/admin/referentiels";
    }

    // ─────────────────────── Helper ───────────────────────

    private <T> List<T> parser(Class<T> clazz, MultipartFile fichier, String format) throws Exception {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier sélectionné");
        }
        try {
            if ("excel".equalsIgnoreCase(format)) {
                return ImportService.importerExcel(clazz, fichier, 0);
            } else {
                return ImportService.importerCsv(clazz, fichier, ';');
            }
        } catch (ImportException e) {
            throw new IllegalArgumentException("Erreur de format : " + e.getMessage());
        }
    }
}
