package com.example.orose.service;

import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.SuiviHebdoBassin;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.SuiviHebdoBassinRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class AlerteBiologiqueScheduler {

    private static final BigDecimal SEUIL_POIDS_RECOLTE = new BigDecimal("15");
    private static final BigDecimal SEUIL_TAILLE_RECOLTE = new BigDecimal("110");
    private static final BigDecimal SEUIL_SURVIE_CRITIQUE = new BigDecimal("40");
    private static final BigDecimal SEUIL_MORTALITE_ORANGE = new BigDecimal("1.0");
    private static final BigDecimal SEUIL_MORTALITE_ROUGE = new BigDecimal("2.0");

    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final SuiviHebdoBassinRepository suiviHebdoBassinRepository;
    private final AlerteService alerteService;

    public AlerteBiologiqueScheduler(CycleBassinAssocRepository cycleBassinAssocRepository,
                                     SuiviHebdoBassinRepository suiviHebdoBassinRepository,
                                     AlerteService alerteService) {
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.suiviHebdoBassinRepository = suiviHebdoBassinRepository;
        this.alerteService = alerteService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void verifierAlertesBiologiques() {
        List<CycleBassinAssoc> assocs = cycleBassinAssocRepository.findByEstClotureFalse();

        for (CycleBassinAssoc assoc : assocs) {
            verifierPeseeManquante(assoc);
            verifierMortaliteAnormale(assoc);
            verifierSurvieCritique(assoc);
            verifierRecoltePossible(assoc);
        }
    }

    private void verifierPeseeManquante(CycleBassinAssoc assoc) {
        Optional<SuiviHebdoBassin> dernierePesee = suiviHebdoBassinRepository
                .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());

        boolean peseeManquante = dernierePesee.isEmpty()
                || dernierePesee.get().getDateSuivi().isBefore(LocalDate.now().minusDays(7));

        if (peseeManquante) {
            alerteService.creerAlerteSiAbsente(assoc.getId(), "PESEE_MANQUANTE", "ORANGE",
                    "Aucune pesée depuis plus de 7 jours pour bassin " + assoc.getBassin().getCode());
        }
    }

    private void verifierMortaliteAnormale(CycleBassinAssoc assoc) {
        Integer semaine = assoc.getSemaineActuelle();
        if (semaine == null || semaine < 8) {
            return;
        }

        List<SuiviHebdoBassin> deuxDernieres = suiviHebdoBassinRepository
                .findTop2ByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId(), PageRequest.of(0, 2));

        if (deuxDernieres.size() < 2) {
            return;
        }

        SuiviHebdoBassin peseeActuelle = deuxDernieres.get(0);
        SuiviHebdoBassin peseePrecedente = deuxDernieres.get(1);
        BigDecimal mortaliteHebdo = calculerMortaliteHebdo(peseeActuelle, peseePrecedente);

        if (mortaliteHebdo.compareTo(SEUIL_MORTALITE_ROUGE) > 0) {
            alerteService.creerAlerteSiAbsente(assoc.getId(), "MORTALITE_ANORMALE", "ROUGE",
                    "Mortalité hebdomadaire anormale (" + mortaliteHebdo.setScale(2, RoundingMode.HALF_UP)
                            + "%) pour bassin " + assoc.getBassin().getCode());
        } else if (mortaliteHebdo.compareTo(SEUIL_MORTALITE_ORANGE) > 0) {
            alerteService.creerAlerteSiAbsente(assoc.getId(), "MORTALITE_ANORMALE", "ORANGE",
                    "Mortalité hebdomadaire élevée (" + mortaliteHebdo.setScale(2, RoundingMode.HALF_UP)
                            + "%) pour bassin " + assoc.getBassin().getCode());
        }
    }

    private void verifierSurvieCritique(CycleBassinAssoc assoc) {
        Optional<SuiviHebdoBassin> dernierePesee = suiviHebdoBassinRepository
                .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());

        if (dernierePesee.isEmpty() || assoc.getEffectifInitial() == null || assoc.getEffectifInitial() == 0) {
            return;
        }

        BigDecimal tauxSurvie = BigDecimal.valueOf(dernierePesee.get().getNbVivants())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(assoc.getEffectifInitial()), 2, RoundingMode.HALF_UP);

        if (tauxSurvie.compareTo(SEUIL_SURVIE_CRITIQUE) < 0) {
            alerteService.creerAlerteSiAbsente(assoc.getId(), "SURVIE_CRITIQUE", "ROUGE",
                    "Taux de survie critique : " + tauxSurvie.setScale(1, RoundingMode.HALF_UP)
                            + "% pour bassin " + assoc.getBassin().getCode());
        }
    }

    private void verifierRecoltePossible(CycleBassinAssoc assoc) {
        Optional<SuiviHebdoBassin> dernierePesee = suiviHebdoBassinRepository
                .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());

        if (dernierePesee.isPresent()) {
            SuiviHebdoBassin pesee = dernierePesee.get();
            if (pesee.getPoidsMoyenGramme().compareTo(SEUIL_POIDS_RECOLTE) >= 0
                    && pesee.getTailleMoyenneMm().compareTo(SEUIL_TAILLE_RECOLTE) >= 0) {
                alerteService.creerAlerteDeRecolte(assoc);
            }
        }
    }

    public BigDecimal calculerMortaliteHebdo(SuiviHebdoBassin peseeActuelle, SuiviHebdoBassin peseePrecedente) {
        if (peseePrecedente == null || peseePrecedente.getNbVivants() == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(peseeActuelle.getNbMorts())
                .divide(BigDecimal.valueOf(peseePrecedente.getNbVivants()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
