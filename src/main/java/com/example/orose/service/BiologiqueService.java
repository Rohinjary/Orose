package com.example.orose.service;

import com.example.orose.dto.BassinSuiviDTO;
import com.example.orose.dto.CourbeCroissanceDTO;
import com.example.orose.dto.SuiviBiologiqueDetailDTO;
import com.example.orose.model.Cycle;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.EvolutionHebdoEspece;
import com.example.orose.model.SuiviHebdoBassin;
import com.example.orose.repository.AlerteRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.EvolutionHebdoEspeceRepository;
import com.example.orose.repository.SuiviHebdoBassinRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BiologiqueService {

    private static final BigDecimal SEUIL_POIDS_RECOLTE = new BigDecimal("15");
    private static final BigDecimal SEUIL_TAILLE_RECOLTE = new BigDecimal("110");
    private static final BigDecimal SEUIL_SURVIE_CRITIQUE = new BigDecimal("40");
    private static final BigDecimal FACTEUR_RETARD = new BigDecimal("0.9");

    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final SuiviHebdoBassinRepository suiviHebdoBassinRepository;
    private final AlerteRepository alerteRepository;
    private final EvolutionHebdoEspeceRepository evolutionHebdoEspeceRepository;

    public BiologiqueService(CycleBassinAssocRepository cycleBassinAssocRepository,
                             SuiviHebdoBassinRepository suiviHebdoBassinRepository,
                             AlerteRepository alerteRepository,
                             EvolutionHebdoEspeceRepository evolutionHebdoEspeceRepository) {
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.suiviHebdoBassinRepository = suiviHebdoBassinRepository;
        this.alerteRepository = alerteRepository;
        this.evolutionHebdoEspeceRepository = evolutionHebdoEspeceRepository;
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
            dto.setPoidsMoyenActuel(assoc.getPoidsMoyenActuel());

            Optional<SuiviHebdoBassin> dernierePesee = suiviHebdoBassinRepository
                    .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());

            BigDecimal tauxSurvie = null;
            if (dernierePesee.isPresent()) {
                SuiviHebdoBassin pesee = dernierePesee.get();
                dto.setDateDernierePesee(pesee.getDateSuivi());
                if (assoc.getEffectifInitial() != null && assoc.getEffectifInitial() > 0) {
                    tauxSurvie = BigDecimal.valueOf(pesee.getNbVivants())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(assoc.getEffectifInitial()), 2, RoundingMode.HALF_UP);
                    dto.setTauxSurvie(tauxSurvie);
                }
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
        dto.setNomEspece(cycle.getEspece().getNomCourant());
        dto.setDateDebut(cycle.getDateDebut());
        dto.setDateFinPrevue(cycle.getDateFinPrevue());
        dto.setEffectifInitial(assoc.getEffectifInitial());
        dto.setSemaineActuelle(assoc.getSemaineActuelle());
        dto.setPesees(pesees);
        dto.setAlertesActives(alerteRepository.findByCycleBassinAssocIdAndEstResolueFalse(idCycleBassinAssoc));

        if (dernierePesee.isPresent()) {
            SuiviHebdoBassin pesee = dernierePesee.get();
            dto.setBiomassActuelleKg(pesee.getBiomasseCalculeeKg());
            dto.setPoidsMoyen(pesee.getPoidsMoyenGramme());
            dto.setTailleMoyenne(pesee.getTailleMoyenneMm());

            if (assoc.getEffectifInitial() != null && assoc.getEffectifInitial() > 0) {
                dto.setTauxSurvie(BigDecimal.valueOf(pesee.getNbVivants())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(assoc.getEffectifInitial()), 2, RoundingMode.HALF_UP));
            }

            boolean calibreAtteint = pesee.getPoidsMoyenGramme().compareTo(SEUIL_POIDS_RECOLTE) >= 0
                    && pesee.getTailleMoyenneMm().compareTo(SEUIL_TAILLE_RECOLTE) >= 0;
            dto.setCalibreAtteint(calibreAtteint);

            dto.setBiomasseRecoltableEstimee(
                    BigDecimal.valueOf(pesee.getNbVivants())
                            .multiply(SEUIL_POIDS_RECOLTE)
                            .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));
        }

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
