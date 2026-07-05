package com.example.orose.service;

import com.example.orose.dto.BassinDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.orose.model.Bassin;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.HistoStatutBassin;
import com.example.orose.model.LotCrevette;
import com.example.orose.model.StatutBassin;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.StatutBassinRepository;
import com.example.orose.repository.CycleRepository;
import com.example.orose.repository.HistoStatutBassinRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.repository.SuiviHebdoBassinRepository;
import com.example.orose.repository.stock.LotCrevetteRepository;
import java.util.stream.Collectors;

@Service
public class BassinService {
    private final BassinRepository bassinRepository;
    private final StatutBassinRepository statutBassinRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final StatutBassinService statutBassinService;
    private final HistoStatutBassinRepository histoStatutBassinRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LotCrevetteRepository lotCrevetteRepository;
    private final SuiviHebdoBassinRepository suiviHebdoBassinRepository;

    public BassinService(BassinRepository bassinRepository,
            StatutBassinRepository statutBassinRepository,
            CycleBassinAssocRepository cycleBassinAssocRepository,
            StatutBassinService statutBassinService,
            HistoStatutBassinRepository histoStatutBassinRepository,
            UtilisateurRepository utilisateurRepository,
            LotCrevetteRepository lotCrevetteRepository,
            SuiviHebdoBassinRepository suiviHebdoBassinRepository) {
        this.bassinRepository = bassinRepository;
        this.statutBassinRepository = statutBassinRepository;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.statutBassinService = statutBassinService;
        this.histoStatutBassinRepository = histoStatutBassinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.lotCrevetteRepository = lotCrevetteRepository;
        this.suiviHebdoBassinRepository = suiviHebdoBassinRepository;
    }

    public Bassin creerBassin(BassinDTO dto) {
        if (bassinRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Le code du bassin doit être unique");
        }

        StatutBassin statutInitial = statutBassinRepository.findByCode("VIDE")
                .orElseThrow(() -> new IllegalArgumentException("Statut VIDE introuvable"));

        Bassin bassin = new Bassin();
        bassin.setCode(dto.getCode());
        bassin.setSurfaceM2(dto.getSurface_m2());
        bassin.setNotes(dto.getNotes());
        bassin.setProfondeurMetre(dto.getProfondeur_metre());
        bassin.setStatutActuel(statutInitial);
        bassin.setCreatedAt(dto.getDateCreation().atStartOfDay());
        bassin = bassinRepository.save(bassin);

        Utilisateur utilisateur = utilisateurRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        HistoStatutBassin histo = new HistoStatutBassin();
        histo.setBassin(bassin);
        histo.setStatutBassin(bassin.getStatutActuel());
        histo.setUtilisateur(utilisateur);
        histo.setMotif("Création du bassin");
        histoStatutBassinRepository.save(histo);

        return bassin;
    }

    public void supprimerBassin(Long id) {
        Bassin bassin = bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));

        // Vérifier qu'aucun cycle actif n'est associé
        if (cycleBassinAssocRepository.existsByBassinIdAndEstClotureFalse(id)) {
            throw new IllegalStateException("Impossible de désactiver le bassin car il est associé à un cycle en cours");
        }

        changerStatutBassin(id, "INACTIF", "Bassin désactivé", 1L);
    }

    public Bassin modifierBassin(Long id, BassinDTO dto) {
        Bassin bassin = bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));

        if (bassinRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
                throw new IllegalArgumentException("Le code du bassin doit être unique");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        List<String> changements = new ArrayList<>();

        if (!bassin.getCode().equals(dto.getCode())) {
                changements.add("code : " + bassin.getCode() + " → " + dto.getCode());
        }
        if (bassin.getSurfaceM2().compareTo(dto.getSurface_m2()) != 0) {
                changements.add("surface : " + bassin.getSurfaceM2() + " m² → " + dto.getSurface_m2() + " m²");
        }
        if (bassin.getProfondeurMetre().compareTo(dto.getProfondeur_metre()) != 0) {
                changements.add("profondeur : " + bassin.getProfondeurMetre() + " m → " + dto.getProfondeur_metre() + " m");
        }
        if (!Objects.equals(bassin.getNotes(), dto.getNotes())) {
                changements.add("notes modifiées");
        }
        if (!bassin.getCreatedAt().toLocalDate().equals(dto.getDateCreation())) {
                changements.add("date de création : " + bassin.getCreatedAt().toLocalDate() + " → " + dto.getDateCreation());
        }

        bassin.setCode(dto.getCode());
        bassin.setSurfaceM2(dto.getSurface_m2());
        bassin.setProfondeurMetre(dto.getProfondeur_metre());
        bassin.setNotes(dto.getNotes());
        bassin.setCreatedAt(dto.getDateCreation().atStartOfDay());

        if (!changements.isEmpty()) {
                String motif = "Modification : " + String.join(" ; ", changements);

                HistoStatutBassin histo = new HistoStatutBassin();
                histo.setBassin(bassin);
                histo.setUtilisateur(utilisateur);
                histo.setStatutBassin(bassin.getStatutActuel());
                histo.setMotif(motif);
                histoStatutBassinRepository.save(histo);
        }

        return bassinRepository.save(bassin);
        }

    public List<Bassin> listerBassins() {
        List<Bassin> bassins = bassinRepository.findAllByOrderByCodeAsc();

        bassins.forEach(b -> System.out.println(b.getCode()));

        return bassins;
    }

    public Bassin getBassinById(Long id) {
        return bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));
    }

    public void changerStatutBassin(Long idBassin, String nouveauStatut, String motif, Long idUtilisateur) {
        Bassin bassin = getBassinById(idBassin);
        String statutActuelCode = bassin.getStatutActuel().getCode();

        // statutBassinService.validerTransition(statutActuelCode, nouveauStatut);

        StatutBassin nouveauStatutEntity = statutBassinRepository.findByCode(nouveauStatut)
                .orElseThrow(() -> new IllegalArgumentException("Nouveau statut introuvable"));

        bassin.setStatutActuel(nouveauStatutEntity);
        bassinRepository.save(bassin);

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        HistoStatutBassin histo = new HistoStatutBassin();
        histo.setBassin(bassin);
        histo.setStatutBassin(nouveauStatutEntity);
        histo.setUtilisateur(utilisateur);
        histo.setMotif(motif);
        histoStatutBassinRepository.save(histo);

        // NOTE: Création de lots déplacée vers BiologiqueRecolteService
        // Celle-ci crée automatiquement un LotCrevette lié à RecolteDeclaration
        // lors de la validation d'une déclaration de récolte.
        // if ("RECOLTE".equals(nouveauStatut)) {
        //     creerLotCrevettePourRecolte(bassin, utilisateur);
        // }
    }

    /**
     * DÉSACTIVÉ : Cette méthode appartient à l'ancien workflow.
     * La création de lots se fait maintenant via BiologiqueRecolteService.crierLotCrevette()
     * lors de la validation d'une déclaration de récolte (recolte_declaration).
     */
    @Deprecated
    private void creerLotCrevettePourRecolte(Bassin bassin, Utilisateur utilisateur) {
        List<CycleBassinAssoc> actifs = cycleBassinAssocRepository.findByEstClotureFalse();
        CycleBassinAssoc assoc = actifs.stream()
                .filter(a -> a.getBassin().getId().equals(bassin.getId()))
                .findFirst().orElse(null);

        if (assoc == null || assoc.getEffectifInitial() == null || assoc.getEffectifInitial() <= 0) {
            return;
        }

        var dernierePesee = suiviHebdoBassinRepository
                .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());

        java.math.BigDecimal poidsMoyen = (assoc.getPoidsMoyenActuel() != null
                && assoc.getPoidsMoyenActuel().compareTo(java.math.BigDecimal.ZERO) > 0)
                ? assoc.getPoidsMoyenActuel()
                : dernierePesee.map(s -> s.getPoidsMoyenGramme())
                        .orElse(java.math.BigDecimal.valueOf(20));

        // NOTE: nb_vivants a été supprimé de suivi_hebdo_bassin.
        // On utilise l'effectif initial comme référence de population.
        Integer population = assoc.getEffectifInitial();

        java.math.BigDecimal biomasse = java.math.BigDecimal.valueOf(population)
                .multiply(poidsMoyen)
                .divide(java.math.BigDecimal.valueOf(1000), 2, java.math.RoundingMode.HALF_UP);

        java.math.BigDecimal tailleMoyenne = dernierePesee
                .map(s -> s.getTailleMoyenneMm() != null ? s.getTailleMoyenneMm() : java.math.BigDecimal.ZERO)
                .orElse(java.math.BigDecimal.ZERO);

        LotCrevette lot = new LotCrevette();
        lot.setNumeroLotUnique(String.format("LOT-%s-%tY%<tm%<td-%03d",
                bassin.getCode(), LocalDate.now(), new Random().nextInt(1000)));
        // lot.setCycleBassinAssoc(assoc);  // SUPPRIMÉ : utiliser recolteDeclaration maintenant
        // lot.setBiomasseTotaleKg(biomasse);  // SUPPRIMÉ : calculée depuis recolteDeclaration.recolteReelleKg
        // lot.setBiomasseActuelleKg(biomasse);  // SUPPRIMÉ : calculée depuis mouvements
        lot.setPoidsMoyenFinalG(poidsMoyen);
        lot.setTailleMoyenneFinaleMm(tailleMoyenne);
        lot.setDateRecolte(LocalDate.now());
        lot.setResponsable(utilisateur);
        lotCrevetteRepository.save(lot);

        assoc.setEstCloture(true);
        assoc.setDateFinReelle(LocalDate.now());
        cycleBassinAssocRepository.save(assoc);
    }

    public List<String> getTransitionsAutorisees(Long idBassin) {
        Bassin bassin = getBassinById(idBassin);
        String statutActuelCode = bassin.getStatutActuel().getCode();
        return statutBassinService.getTransitionsAutorisees(statutActuelCode);
    }

    public List<HistoStatutBassin> getHistoriqueStatuts(Long idBassin, LocalDateTime debut, LocalDateTime fin,
            String typeEtat) {
        return histoStatutBassinRepository.findByBassinIdOrderByDateChangementDesc(idBassin).stream()
                .filter(histo -> debut == null || !histo.getDateChangement().isBefore(debut))
                .filter(histo -> fin == null || !histo.getDateChangement().isAfter(fin))
                .filter(histo -> typeEtat == null || typeEtat.isBlank()
                        || (histo.getStatutBassin() != null && typeEtat.equals(histo.getStatutBassin().getCode())))
                .collect(Collectors.toList());
    }

    public HistoStatutBassin getDernierStatut(Long idBassin) {
        return histoStatutBassinRepository.findTopByBassinIdOrderByDateChangementDesc(idBassin)
                .orElseThrow(() -> new IllegalArgumentException("Aucun historique trouvé pour ce bassin"));
    }

    public List<Bassin> getBassinsParStatut(String codeStatut) {
        return bassinRepository.findAll().stream()
                .filter(b -> codeStatut.equals(b.getStatutActuel().getCode()))
                .collect(Collectors.toList());
    }

    public List<HistoStatutBassin> getHistoriqueGlobal(LocalDateTime debut, LocalDateTime fin, String typeEtat, String q) {
        return histoStatutBassinRepository.findAllByOrderByDateChangementDesc().stream()
                .filter(h -> debut == null || !h.getDateChangement().isBefore(debut))
                .filter(h -> fin == null || !h.getDateChangement().isAfter(fin))
                .filter(h -> typeEtat == null || typeEtat.isBlank()
                        || (h.getStatutBassin() != null && typeEtat.equals(h.getStatutBassin().getCode())))
                .filter(h -> q == null || q.isBlank()
                        || (h.getBassin() != null && h.getBassin().getCode() != null
                                && h.getBassin().getCode().toLowerCase().contains(q.toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<Bassin> getBassinsActifsEtEnTraitement() {
        List<Bassin> bassins = bassinRepository.findByStatutActuel_CodeIn(
                List.of("ACTIF", "EN_TRAITEMENT"));
        return bassins;
    }
}
