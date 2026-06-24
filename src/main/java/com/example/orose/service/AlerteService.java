package com.example.orose.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orose.model.Alerte;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.AlerteRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.UtilisateurRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AlerteService {

    private static final BigDecimal SEUIL_VARIATION_BIOMASSE = new BigDecimal("30");

    private final AlerteRepository alerteRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final UtilisateurRepository utilisateurRepository;

    public AlerteService(AlerteRepository alerteRepository,
                         CycleBassinAssocRepository cycleBassinAssocRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.alerteRepository = alerteRepository;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Alerte> getAlertesBiologiques() {
        return alerteRepository.findByModuleSourceAndEstResolueFalseOrderByDateCreationDesc("BIOLOGIQUE");
    }

    public void resoudreAlerte(Long idAlerte, Long idUtilisateur) {
        Alerte alerte = alerteRepository.findById(idAlerte)
                .orElseThrow(() -> new EntityNotFoundException("Alerte introuvable : " + idAlerte));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + idUtilisateur));

        alerte.setEstResolue(true);
        alerte.setDateResolution(LocalDateTime.now());
        alerte.setResoluPar(utilisateur);
        alerteRepository.save(alerte);
    }

    public void creerAlerteSiAbsente(Integer idCycleBassinAssoc, String typeAlerte,
                                     String niveau, String message) {
        boolean existe = alerteRepository.existsByCycleBassinAssocIdAndTypeAlerteAndEstResolueFalse(
                idCycleBassinAssoc, typeAlerte);
        if (!existe) {
            CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(idCycleBassinAssoc.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("CycleBassinAssoc introuvable : " + idCycleBassinAssoc));

            Alerte alerte = new Alerte();
            alerte.setCycleBassinAssoc(assoc);
            alerte.setTypeAlerte(typeAlerte);
            alerte.setNiveau(niveau);
            alerte.setModuleSource("BIOLOGIQUE");
            alerte.setMessage(message);
            alerte.setEstResolue(false);
            alerteRepository.save(alerte);
        }
    }

    public void creerAlerteDeRecolte(CycleBassinAssoc assoc) {
        creerAlerteSiAbsente(assoc.getId(), "RECOLTE_POSSIBLE", "VERT",
                "Calibre de récolte atteint pour le bassin " + assoc.getBassin().getCode()
                        + " (cycle " + assoc.getCycle().getCodeUniqueCycle() + ")");
    }

    /**
     * Vérifie si la variation de biomasse entre deux pesées dépasse le seuil de 30%.
     * Crée une alerte ORANGE si c'est le cas, en précisant le sens (hausse ou baisse).
     *
     * @param assoc          l'association cycle-bassin concernée
     * @param biomassePrec   biomasse calculée de la pesée précédente (kg)
     * @param biomasseNouvelle biomasse calculée de la nouvelle pesée (kg)
     */
    public void verifierVariationBiomasse(CycleBassinAssoc assoc,
                                          BigDecimal biomassePrec,
                                          BigDecimal biomasseNouvelle) {
        if (biomassePrec == null || biomasseNouvelle == null
                || biomassePrec.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // variation (%) = (nouvelle - précédente) / précédente × 100
        BigDecimal variation = biomasseNouvelle
                .subtract(biomassePrec)
                .multiply(new BigDecimal("100"))
                .divide(biomassePrec, 2, RoundingMode.HALF_UP)
                .abs();

        if (variation.compareTo(SEUIL_VARIATION_BIOMASSE) >= 0) {
            String sens = biomasseNouvelle.compareTo(biomassePrec) > 0 ? "hausse" : "baisse";
            String message = String.format(
                    "Variation anormale de biomasse détectée sur le bassin %s (cycle %s) : %s%% de %s "
                            + "(%.2f kg → %.2f kg). Vérifier les données de pesée.",
                    assoc.getBassin().getCode(),
                    assoc.getCycle().getCodeUniqueCycle(),
                    variation.toPlainString(),
                    sens,
                    biomassePrec,
                    biomasseNouvelle);

            // On crée une nouvelle alerte à chaque pesée anormale (pas de déduplication
            // par typeAlerte seul, car le contexte chiffré change à chaque fois).
            CycleBassinAssoc managed = cycleBassinAssocRepository
                    .findById(assoc.getId().longValue())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "CycleBassinAssoc introuvable : " + assoc.getId()));

            Alerte alerte = new Alerte();
            alerte.setCycleBassinAssoc(managed);
            alerte.setTypeAlerte("VARIATION_BIOMASSE");
            alerte.setNiveau("ORANGE");
            alerte.setModuleSource("BIOLOGIQUE");
            alerte.setMessage(message);
            alerte.setEstResolue(false);
            alerteRepository.save(alerte);
        }
    }
}