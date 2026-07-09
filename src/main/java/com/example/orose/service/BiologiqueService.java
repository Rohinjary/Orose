package com.example.orose.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orose.dto.BassinSuiviDTO;
import com.example.orose.dto.CourbeCroissanceDTO;
import com.example.orose.dto.SuiviBiologiqueDetailDTO;
import com.example.orose.dto.SurvieBassinDTO;
import com.example.orose.model.Cycle;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.EvolutionHebdoEspece;
import com.example.orose.model.SuiviHebdoBassin;
import com.example.orose.repository.AlerteRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.EvolutionHebdoEspeceRepository;
import com.example.orose.repository.RecolteDeclarationRepository;
import com.example.orose.repository.SuiviHebdoBassinRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BiologiqueService {

    private static final BigDecimal SEUIL_POIDS_RECOLTE = new BigDecimal("20");
    private static final BigDecimal SEUIL_SURVIE_CRITIQUE = new BigDecimal("40");
    private static final BigDecimal FACTEUR_RETARD = new BigDecimal("0.9");
    private static final BigDecimal SEUIL_TAILLE_RECOLTE = new BigDecimal("100");

    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final SuiviHebdoBassinRepository suiviHebdoBassinRepository;
    private final AlerteRepository alerteRepository;
    private final EvolutionHebdoEspeceRepository evolutionHebdoEspeceRepository;
    private final RecolteDeclarationRepository recolteDeclarationRepository;
    private final BassinService bassinService;

    public BiologiqueService(CycleBassinAssocRepository cycleBassinAssocRepository,
                             SuiviHebdoBassinRepository suiviHebdoBassinRepository,
                             AlerteRepository alerteRepository,
                             EvolutionHebdoEspeceRepository evolutionHebdoEspeceRepository,
                             RecolteDeclarationRepository recolteDeclarationRepository,
                             BassinService bassinService) {
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.suiviHebdoBassinRepository = suiviHebdoBassinRepository;
        this.alerteRepository = alerteRepository;
        this.evolutionHebdoEspeceRepository = evolutionHebdoEspeceRepository;
        this.recolteDeclarationRepository = recolteDeclarationRepository;
        this.bassinService = bassinService;
    }

    /**
     * Seule voie autorisée pour faire passer un bassin en statut RECOLTE :
     * le calibre de récolte (poids + taille de la dernière pesée) doit être atteint.
     */
    public void declarerRecolte(Integer idCycleBassinAssoc, String motif, Long idUtilisateur) {
        CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(idCycleBassinAssoc.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Association cycle-bassin introuvable : " + idCycleBassinAssoc));

        if (Boolean.TRUE.equals(assoc.getEstCloture())) {
            throw new IllegalStateException("Ce cycle est déjà clôturé.");
        }

        SuiviHebdoBassin dernierePesee = suiviHebdoBassinRepository
                .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(idCycleBassinAssoc)
                .orElseThrow(() -> new IllegalStateException("Aucune pesée enregistrée : impossible de vérifier le calibre de récolte."));

        boolean calibreAtteint = dernierePesee.getPoidsMoyenGramme().compareTo(SEUIL_POIDS_RECOLTE) >= 0
                && dernierePesee.getTailleMoyenneMm().compareTo(SEUIL_TAILLE_RECOLTE) >= 0;
        if (!calibreAtteint) {
            throw new IllegalStateException("Le calibre de récolte n'est pas encore atteint (seuils : "
                    + SEUIL_POIDS_RECOLTE + " g / " + SEUIL_TAILLE_RECOLTE + " mm).");
        }

        bassinService.changerStatutBassin(assoc.getBassin().getId().longValue(), "RECOLTE", motif, idUtilisateur);

    }

    public List<SurvieBassinDTO> getStatistiquesSurvieActifs() {
        List<CycleBassinAssoc> assocs = cycleBassinAssocRepository.findAll();

        List<SurvieBassinDTO> result = new ArrayList<>();
        Map<String, BigDecimal> totalsParCycle = new HashMap<>();
        Map<String, Integer> comptesParCycle = new HashMap<>();

        for (CycleBassinAssoc assoc : assocs) {
            SurvieBassinDTO dto = new SurvieBassinDTO();
            dto.setCodeUniqueCycle(assoc.getCycle().getCodeUniqueCycle());
            dto.setCodeBassin(assoc.getBassin().getCode());

            var derniereRecolte = recolteDeclarationRepository
                    .findByIdCycleBassinAssocIdOrderByIdDesc(assoc.getId());

            BigDecimal tauxSurvie = BigDecimal.valueOf(100);
            String etat = "ACTIF";
            if (derniereRecolte.isPresent()) {
                if (derniereRecolte.get().getTauxSurviePercent() != null) {
                    tauxSurvie = derniereRecolte.get().getTauxSurviePercent();
                }
                etat = "RÉCOLTÉ";
            }

            dto.setTauxSurvieBassin(tauxSurvie.setScale(2, RoundingMode.HALF_UP));
            dto.setEtatBassin(etat);
            dto.setCouleur(couleurPourEtat(etat));
            result.add(dto);

            String cycleKey = assoc.getCycle().getCodeUniqueCycle();
            totalsParCycle.merge(cycleKey, dto.getTauxSurvieBassin(), BigDecimal::add);
            comptesParCycle.merge(cycleKey, 1, Integer::sum);
        }

        for (SurvieBassinDTO dto : result) {
            String cycleKey = dto.getCodeUniqueCycle();
            int count = comptesParCycle.getOrDefault(cycleKey, 1);
            BigDecimal moyenneCycle = totalsParCycle.getOrDefault(cycleKey, BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            dto.setTauxSurvieMoyenCycle(moyenneCycle);
        }

        result.sort((a, b) -> {
            int cmp = a.getCodeUniqueCycle().compareToIgnoreCase(b.getCodeUniqueCycle());
            if (cmp != 0) {
                return cmp;
            }
            return a.getCodeBassin().compareToIgnoreCase(b.getCodeBassin());
        });

        Map<String, Integer> rowspansParCycle = new HashMap<>();
        for (SurvieBassinDTO dto : result) {
            rowspansParCycle.merge(dto.getCodeUniqueCycle(), 1, Integer::sum);
        }

        String cycleCourant = null;
        for (SurvieBassinDTO dto : result) {
            if (!java.util.Objects.equals(cycleCourant, dto.getCodeUniqueCycle())) {
                dto.setRowspanCycle(rowspansParCycle.getOrDefault(dto.getCodeUniqueCycle(), 1));
                cycleCourant = dto.getCodeUniqueCycle();
            } else {
                dto.setRowspanCycle(0);
            }
        }

        return result;
    }

    public BigDecimal getTauxSurvieGeneral() {
        List<SurvieBassinDTO> statistiques = getStatistiquesSurvieActifs();
        if (statistiques.isEmpty()) {
            return BigDecimal.valueOf(100);
        }
        BigDecimal total = statistiques.stream()
                .map(SurvieBassinDTO::getTauxSurvieBassin)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(statistiques.size()), 2, RoundingMode.HALF_UP);
    }

    private String couleurPourEtat(String etat) {
        return switch (etat) {
            case "RÉCOLTÉ" -> "#7d8583";
            case "ACTIF" -> "#8af8b2";
            default -> "#64748b";
        };
    }

    public List<BassinSuiviDTO> getBassinsSuivi() {
        List<CycleBassinAssoc> assocs = cycleBassinAssocRepository.findByEstClotureFalse().stream()
                .filter(a -> a.getBassin().getStatutActuel() != null
                        && "ACTIF".equals(a.getBassin().getStatutActuel().getCode()))
                .collect(Collectors.toList());

        List<BassinSuiviDTO> result = new ArrayList<>();
        for (CycleBassinAssoc assoc : assocs) {
            BassinSuiviDTO dto = new BassinSuiviDTO();
            dto.setIdBassin(assoc.getBassin().getId());
            dto.setCodeBassin(assoc.getBassin().getCode());
            dto.setCodeUniqueCycle(assoc.getCycle().getCodeUniqueCycle());
            dto.setIdCycleBassinAssoc(assoc.getId());
            dto.setSemaine(assoc.getSemaineActuelle());

            Optional<SuiviHebdoBassin> dernierePesee = suiviHebdoBassinRepository
                    .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());
            dto.setPoidsMoyenActuel(determinerPoidsMoyenActuel(assoc, dernierePesee));

            BigDecimal tauxSurvie = null;
            if (dernierePesee.isPresent()) {
                SuiviHebdoBassin pesee = dernierePesee.get();
                dto.setDateDernierePesee(pesee.getDateSuivi());
                // REMARQUE: Le taux de survie n'est plus calculé pendant le suivi hebdo.
                // Il est maintenant déterminé lors de la déclaration de récolte (recolte_declaration.taux_survie_percent).
                // Les colonnes nb_vivants et nb_morts ont été supprimées de suivi_hebdo_bassin.
            }

            Integer idEspece = assoc.getCycle().getEspece().getId();
            Integer semaine = assoc.getSemaineActuelle() != null ? assoc.getSemaineActuelle() : 0;
            Optional<EvolutionHebdoEspece> evolution = evolutionHebdoEspeceRepository
                    .findByEspeceIdAndSemaine(idEspece, semaine);

            dto.setStatutCroissance(calculerStatutCroissance(tauxSurvie, assoc.getPoidsMoyenActuel(), evolution));
            result.add(dto);
        }
        return result;
    }

    /**
     * Suivi biologique du cycle actif d'un bassin donné, utilisé par la fiche détail du bassin.
     */
    public Optional<BassinSuiviDTO> getSuiviActifPourBassin(Long idBassin) {
        return cycleBassinAssocRepository.findByBassinIdAndEstClotureFalse(idBassin)
                .map(this::construireSuiviDTO);
    }

    private BassinSuiviDTO construireSuiviDTO(CycleBassinAssoc assoc) {
        BassinSuiviDTO dto = new BassinSuiviDTO();
        dto.setIdBassin(assoc.getBassin().getId());
        dto.setCodeBassin(assoc.getBassin().getCode());
        dto.setCodeUniqueCycle(assoc.getCycle().getCodeUniqueCycle());
        dto.setIdCycleBassinAssoc(assoc.getId());
        dto.setSemaine(assoc.getSemaineActuelle());

        dto.setDateFinPrevue(assoc.getCycle().getDateFinPrevue());

        LocalDate aujourdhui = LocalDate.now();
        LocalDate dateDebut = assoc.getCycle().getDateDebut();
        LocalDate dateFinPrevue = assoc.getCycle().getDateFinPrevue();
        if (dateFinPrevue != null) {
            dto.setJoursRestants(Math.max(0, ChronoUnit.DAYS.between(aujourdhui, dateFinPrevue)));
        }
        if (dateDebut != null && dateFinPrevue != null && dateFinPrevue.isAfter(dateDebut)) {
            long dureeTotaleJours = ChronoUnit.DAYS.between(dateDebut, dateFinPrevue);
            long joursEcoules = ChronoUnit.DAYS.between(dateDebut, aujourdhui);
            int avancement = (int) Math.round(Math.min(100.0, Math.max(0.0, joursEcoules * 100.0 / dureeTotaleJours)));
            dto.setTauxAvancement(avancement);
        }

        Optional<SuiviHebdoBassin> dernierePesee = suiviHebdoBassinRepository
                .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());

        BigDecimal tauxSurvie = null;
        if (dernierePesee.isPresent()) {
            SuiviHebdoBassin pesee = dernierePesee.get();
            dto.setDateDernierePesee(pesee.getDateSuivi());
            dto.setPoidsMoyenActuel(determinerPoidsMoyenActuel(assoc, dernierePesee));
            // NOTE: Le taux de survie n'est plus calculé pendant le suivi (nb_vivants supprimé).
            // Il sera déterminé lors de la déclaration de récolte via recolte_declaration.taux_survie_percent
            tauxSurvie = null;
        } else {
            dto.setPoidsMoyenActuel(assoc.getPoidsMoyenActuel());
        }

        Integer idEspece = assoc.getCycle().getEspece().getId();
        Integer semaine = assoc.getSemaineActuelle() != null ? assoc.getSemaineActuelle() : 0;
        Optional<EvolutionHebdoEspece> evolution = evolutionHebdoEspeceRepository
                .findByEspeceIdAndSemaine(idEspece, semaine);

        dto.setStatutCroissance(calculerStatutCroissance(tauxSurvie, assoc.getPoidsMoyenActuel(), evolution));

        BigDecimal poidsMoyenActuel = assoc.getPoidsMoyenActuel();
        BigDecimal seuilProche = SEUIL_POIDS_RECOLTE.multiply(new BigDecimal("0.8"));
        dto.setRecolteProche(poidsMoyenActuel != null
                && poidsMoyenActuel.compareTo(seuilProche) >= 0
                && poidsMoyenActuel.compareTo(SEUIL_POIDS_RECOLTE) < 0);

        return dto;
    }

    public SuiviBiologiqueDetailDTO getDetailBiologique(Integer idCycleBassinAssoc) {
        CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(idCycleBassinAssoc.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Association cycle-bassin introuvable : " + idCycleBassinAssoc));

        Cycle cycle = assoc.getCycle();
        List<SuiviHebdoBassin> pesees = suiviHebdoBassinRepository
                .findByCycleBassinAssocIdOrderByDateSuiviAsc(idCycleBassinAssoc);
        Optional<SuiviHebdoBassin> dernierePesee = pesees.isEmpty()
                ? Optional.empty()
                : Optional.of(pesees.get(pesees.size() - 1));

        SuiviBiologiqueDetailDTO dto = new SuiviBiologiqueDetailDTO();
        dto.setIdCycleBassinAssoc(assoc.getId());
        dto.setCodeUniqueCycle(cycle.getCodeUniqueCycle());
        dto.setCodeBassin(assoc.getBassin().getCode());
        if (assoc.getBassin().getStatutActuel() != null) {
            dto.setStatutBassinCode(assoc.getBassin().getStatutActuel().getCode());
            dto.setStatutBassinLibelle(assoc.getBassin().getStatutActuel().getLibelle());
        }
        dto.setNomEspece(cycle.getEspece().getNomCourant());
        dto.setDateDebut(cycle.getDateDebut());
        dto.setDateFinPrevue(cycle.getDateFinPrevue());
        dto.setEffectifInitial(assoc.getEffectifInitial());
        dto.setSemaineActuelle(assoc.getSemaineActuelle());
        dto.setPesees(pesees);
        dto.setAlertesActives(alerteRepository.findByCycleBassinAssocIdAndEstResolueFalse(idCycleBassinAssoc));

        if (dernierePesee.isPresent()) {
            SuiviHebdoBassin pesee = dernierePesee.get();
            // NOTE: getBiomasseCalculeeKg() n'existe plus - supprimé avec nb_vivants et nb_morts
            // dto.setBiomassActuelleKg(pesee.getBiomasseCalculeeKg());
            dto.setPoidsMoyen(determinerPoidsMoyenActuel(assoc, Optional.of(pesee)));
            dto.setTailleMoyenne(pesee.getTailleMoyenneMm());

            // NOTE: Le taux de survie n'est plus calculé pendant le suivi.
            // Il sera déterminé lors de la déclaration de récolte.
            // if (assoc.getEffectifInitial() != null && assoc.getEffectifInitial() > 0) {
            //     dto.setTauxSurvie(BigDecimal.valueOf(pesee.getNbVivants())...);
            // }

            // NOTE: La biomasse recoltable estimée ne peut plus être calculée
            // car le nombre de vivants n'est plus enregistré.
            // dto.setBiomasseRecoltableEstimee(...);
        }

        dto.setCalibreAtteint(estRecoltableParPoids(pesees));

        List<EvolutionHebdoEspece> courbeStandardData = evolutionHebdoEspeceRepository
                .findByEspeceIdOrderBySemaineAsc(cycle.getEspece().getId());

        if (!dto.isCalibreAtteint()) {
            dto.setDateEstimeeRecolte(calculerDateEstimeeRecolte(cycle.getDateDebut(), courbeStandardData));
        }

        dto.setCourbeReelle(pesees.stream().map(p -> {
            CourbeCroissanceDTO point = new CourbeCroissanceDTO();
            point.setSemaine(p.getSemaineActuelle());
            point.setPoidsMoyenG(p.getPoidsMoyenGramme());
            point.setTailleMoyenneMm(p.getTailleMoyenneMm());
            return point;
        }).collect(Collectors.toList()));

        int semaineMax = (assoc.getSemaineActuelle() != null ? assoc.getSemaineActuelle() : 0) + 4;
        dto.setCourbeStandard(courbeStandardData.stream()
                .filter(e -> e.getSemaine() <= semaineMax)
                .map(e -> {
                    CourbeCroissanceDTO point = new CourbeCroissanceDTO();
                    point.setSemaine(e.getSemaine());
                    point.setPoidsMoyenG(e.getPoidsCibleG());
                    point.setTailleMoyenneMm(e.getTailleCibleMm());
                    return point;
                }).collect(Collectors.toList()));

        return dto;
    }

    @Transactional
    public void recolterSiCalibreAtteint(Integer idCycleBassinAssoc, Long idUtilisateur) {
        CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(idCycleBassinAssoc.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Association cycle-bassin introuvable : " + idCycleBassinAssoc));

        if (assoc.getBassin().getStatutActuel() != null
                && "RECOLTE".equals(assoc.getBassin().getStatutActuel().getCode())) {
            throw new IllegalStateException("Ce bassin est deja en statut RECOLTE");
        }

        List<SuiviHebdoBassin> pesees = suiviHebdoBassinRepository
                .findByCycleBassinAssocIdOrderByDateSuiviAsc(idCycleBassinAssoc);
        if (!estRecoltableParPoids(pesees)) {
            throw new IllegalStateException("La recolte est autorisee uniquement si une pesee atteint au moins 20 g");
        }

        bassinService.changerStatutBassin(
                assoc.getBassin().getId().longValue(),
                "RECOLTE",
                "Recolte declenchee depuis le suivi biologique (poids moyen >= 20 g)",
                idUtilisateur);
    }

    private boolean estRecoltableParPoids(List<SuiviHebdoBassin> pesees) {
        return pesees.stream()
                .anyMatch(p -> p.getPoidsMoyenGramme() != null
                        && p.getPoidsMoyenGramme().compareTo(SEUIL_POIDS_RECOLTE) >= 0);
    }

    private BigDecimal determinerPoidsMoyenActuel(CycleBassinAssoc assoc, Optional<SuiviHebdoBassin> dernierePesee) {
        if (dernierePesee.isPresent() && dernierePesee.get().getPoidsMoyenGramme() != null) {
            return dernierePesee.get().getPoidsMoyenGramme();
        }
        return assoc.getPoidsMoyenActuel();
    }

    private String calculerStatutCroissance(BigDecimal tauxSurvie, BigDecimal poidsMoyenActuel,
                                          Optional<EvolutionHebdoEspece> evolution) {
        if (tauxSurvie != null && tauxSurvie.compareTo(SEUIL_SURVIE_CRITIQUE) < 0) {
            return "CRITIQUE";
        }
        if (evolution.isPresent() && poidsMoyenActuel != null) {
            BigDecimal seuilRetard = evolution.get().getPoidsCibleG().multiply(FACTEUR_RETARD);
            if (poidsMoyenActuel.compareTo(seuilRetard) < 0) {
                return "RETARD";
            }
        }
        return "NORMAL";
    }

    private LocalDate calculerDateEstimeeRecolte(LocalDate dateDebut, List<EvolutionHebdoEspece> courbeStandard) {
        for (EvolutionHebdoEspece evolution : courbeStandard) {
            if (evolution.getPoidsCibleG().compareTo(SEUIL_POIDS_RECOLTE) >= 0) {
                return dateDebut.plusWeeks(evolution.getSemaine());
            }
        }
        return null;
    }
}
