package com.example.orose.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orose.dto.PeseeDTO;
import com.example.orose.model.Cycle;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.SuiviHebdoBassin;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.SuiviHebdoBassinRepository;
import com.example.orose.repository.UtilisateurRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class PeseeService {

    private static final BigDecimal SEUIL_POIDS_RECOLTE  = new BigDecimal("15");
    private static final BigDecimal SEUIL_TAILLE_RECOLTE = new BigDecimal("110");

    private final SuiviHebdoBassinRepository suiviHebdoBassinRepository;
    private final CycleBassinAssocRepository  cycleBassinAssocRepository;
    private final UtilisateurRepository       utilisateurRepository;
    private final AlerteService               alerteService;

    public PeseeService(SuiviHebdoBassinRepository suiviHebdoBassinRepository,
                        CycleBassinAssocRepository cycleBassinAssocRepository,
                        UtilisateurRepository utilisateurRepository,
                        AlerteService alerteService) {
        this.suiviHebdoBassinRepository = suiviHebdoBassinRepository;
        this.cycleBassinAssocRepository  = cycleBassinAssocRepository;
        this.utilisateurRepository       = utilisateurRepository;
        this.alerteService               = alerteService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENREGISTREMENT
    // ─────────────────────────────────────────────────────────────────────────

    public SuiviHebdoBassin enregistrerPesee(PeseeDTO dto) {

        CycleBassinAssoc assoc = cycleBassinAssocRepository
                .findById(dto.getIdCycleBassinAssoc().longValue())
                .orElseThrow(() -> new EntityNotFoundException("Association cycle-bassin introuvable"));

        validerEtatAssoc(assoc);

        // ── Pesée précédente (pour validation date ET contrôle biomasse) ───
        Optional<SuiviHebdoBassin> peseePrecedenteOpt =
                suiviHebdoBassinRepository.findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());

        validerDonneesPesee(dto, assoc, peseePrecedenteOpt);

        Cycle cycle = assoc.getCycle();
        int semaineActuelle = calculerSemaine(cycle.getDateDebut(), dto.getDateSuivi());

        Utilisateur technicien = utilisateurRepository.findById(dto.getIdTechnicien())
                .orElseThrow(() -> new EntityNotFoundException("Technicien introuvable"));

        SuiviHebdoBassin pesee = new SuiviHebdoBassin();
        pesee.setCycleBassinAssoc(assoc);
        pesee.setDateSuivi(dto.getDateSuivi());
        pesee.setSemaineActuelle(semaineActuelle);
        pesee.setPoidsMoyenGramme(dto.getPoidsMoyenGramme());
        pesee.setTailleMoyenneMm(dto.getTailleMoyenneMm());
        pesee.setNbVivants(dto.getNbVivants());
        pesee.setNbMorts(dto.getNbMorts() != null ? dto.getNbMorts() : 0);
        pesee.setTechnicien(technicien);
        pesee.setNotes(dto.getNotes());

        SuiviHebdoBassin saved = suiviHebdoBassinRepository.save(pesee);

        // ── Mise à jour de l'association ───────────────────────────────────
        assoc.setPoidsMoyenActuel(dto.getPoidsMoyenGramme());
        assoc.setSemaineActuelle(semaineActuelle);
        cycleBassinAssocRepository.save(assoc);

        // ── Alertes ────────────────────────────────────────────────────────
        verifierAlerteRecolte(dto, assoc);
        verifierAlerteBiomasse(assoc, peseePrecedenteOpt, saved);

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODIFICATION
    // ─────────────────────────────────────────────────────────────────────────

    public SuiviHebdoBassin modifierPesee(Long id, PeseeDTO dto) {

        SuiviHebdoBassin pesee = suiviHebdoBassinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pesée introuvable : " + id));

        CycleBassinAssoc assoc = pesee.getCycleBassinAssoc();

        if (Boolean.TRUE.equals(assoc.getEstCloture())) {
            throw new IllegalStateException("Le cycle-bassin est clôturé, impossible de modifier la pesée");
        }
        // ── Pesée précédente = la plus récente hors pesée courante ──────────
        Optional<SuiviHebdoBassin> peseePrecedenteOpt =
                suiviHebdoBassinRepository.findByCycleBassinAssocIdOrderByDateSuiviAsc(assoc.getId())
                        .stream()
                        .filter(p -> !p.getId().equals(id)
                                && !p.getDateSuivi().isAfter(dto.getDateSuivi()))
                        .reduce((a, b) -> b); // dernière avant la date modifiée

        validerDonneesPesee(dto, assoc, peseePrecedenteOpt);

        Cycle cycle = assoc.getCycle();
        int semaineActuelle = calculerSemaine(cycle.getDateDebut(), dto.getDateSuivi());

        Utilisateur technicien = utilisateurRepository.findById(dto.getIdTechnicien())
                .orElseThrow(() -> new EntityNotFoundException("Technicien introuvable"));

        pesee.setDateSuivi(dto.getDateSuivi());
        pesee.setSemaineActuelle(semaineActuelle);
        pesee.setPoidsMoyenGramme(dto.getPoidsMoyenGramme());
        pesee.setTailleMoyenneMm(dto.getTailleMoyenneMm());
        pesee.setNbVivants(dto.getNbVivants());
        pesee.setNbMorts(dto.getNbMorts() != null ? dto.getNbMorts() : 0);
        pesee.setTechnicien(technicien);
        pesee.setNotes(dto.getNotes());

        SuiviHebdoBassin saved = suiviHebdoBassinRepository.save(pesee);

        // ── Mise à jour de l'association si c'est la pesée la plus récente ─
        Optional<SuiviHebdoBassin> derniere = suiviHebdoBassinRepository
                .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());
        if (derniere.isPresent() && derniere.get().getId().equals(saved.getId())) {
            assoc.setPoidsMoyenActuel(dto.getPoidsMoyenGramme());
            assoc.setSemaineActuelle(semaineActuelle);
            cycleBassinAssocRepository.save(assoc);
            verifierAlerteRecolte(dto, assoc);
        }

        // ── Alerte biomasse (toujours vérifiée, même en modification) ──────
        verifierAlerteBiomasse(assoc, peseePrecedenteOpt, saved);

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MÉTHODES UTILITAIRES PUBLIQUES
    // ─────────────────────────────────────────────────────────────────────────

    public List<SuiviHebdoBassin> getPeseesByCycleBassinAssoc(Integer idCycleBassinAssoc) {
        return suiviHebdoBassinRepository.findByCycleBassinAssocIdOrderByDateSuiviAsc(idCycleBassinAssoc);
    }

    public Optional<SuiviHebdoBassin> getDernierePesee(Integer idCycleBassinAssoc) {
        return suiviHebdoBassinRepository.findTopByCycleBassinAssocIdOrderByDateSuiviDesc(idCycleBassinAssoc);
    }

    public SuiviHebdoBassin getPeseeById(Long id) {
        return suiviHebdoBassinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pesée introuvable : " + id));
    }

    /**
     * Archivage (soft delete) — colonne est_archive absente en base pour l'instant.
     */
    public void archiverPesee(Long id) {
        suiviHebdoBassinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pesée introuvable : " + id));
        throw new UnsupportedOperationException(
                "Archivage non disponible : la colonne est_archive n'existe pas dans la table suivi_hebdo_bassin. "
                        + "Ajouter la colonne en base et dans l'entité SuiviHebdoBassin pour activer cette fonctionnalité.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MÉTHODES PRIVÉES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcule le numéro de semaine du cycle à partir de la date de début.
     * Jour J0 (dateDebut) = S1. Jamais inférieur à 1.
     * Formule : floor(jours écoulés / 7) + 1, borné à 1 minimum.
     *
     * Exemples :
     *   dateDebut=23/06, dateSuivi=23/06 → 0 jours → S1
     *   dateDebut=23/06, dateSuivi=25/06 → 2 jours → S1
     *   dateDebut=23/06, dateSuivi=30/06 → 7 jours → S2
     */
    private int calculerSemaine(LocalDate dateDebut, LocalDate dateSuivi) {
        long joursEcoules = ChronoUnit.DAYS.between(dateDebut, dateSuivi);
        int semaine = (int) (joursEcoules / 7) + 1;
        return Math.max(semaine, 1);
    }

    private void validerEtatAssoc(CycleBassinAssoc assoc) {
        if (Boolean.TRUE.equals(assoc.getEstCloture())) {
            throw new IllegalStateException("Le cycle-bassin est clôturé, impossible d'enregistrer une pesée");
        }
        if (assoc.getBassin().getStatutActuel() == null
                || !"ACTIF".equals(assoc.getBassin().getStatutActuel().getCode())) {
            throw new IllegalStateException("Le bassin doit être en statut ACTIF pour enregistrer une pesée");
        }
    }

    private void validerDonneesPesee(PeseeDTO dto, CycleBassinAssoc assoc,
                                     Optional<SuiviHebdoBassin> dernierePesee) {
        // ── Validation de la date ──────────────────────────────────────────
        if (dto.getDateSuivi() == null) {
            throw new IllegalArgumentException("La date de suivi est obligatoire");
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dateDebut = assoc.getCycle().getDateDebut();
        if (dto.getDateSuivi().isBefore(dateDebut)) {
            throw new IllegalArgumentException(
                    "La date de suivi (" + dto.getDateSuivi().format(fmt)
                    + ") ne peut pas être antérieure au début du cycle ("
                    + dateDebut.format(fmt) + ")");
        }
        if (dto.getDateSuivi().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de suivi ne peut pas être dans le futur");
        }
        if (dernierePesee.isPresent()) {
            LocalDate dateDerniere = dernierePesee.get().getDateSuivi();
            if (!dto.getDateSuivi().isAfter(dateDerniere)) {
                throw new IllegalArgumentException(
                        "La date de suivi doit être postérieure à la dernière pesée enregistrée ("
                        + dateDerniere.format(fmt) + ")");
            }
        }

        // ── Validation des mesures ─────────────────────────────────────────
        if (dto.getPoidsMoyenGramme() == null || dto.getPoidsMoyenGramme().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le poids moyen doit être supérieur à 0");
        }
        if (dto.getTailleMoyenneMm() == null || dto.getTailleMoyenneMm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La taille moyenne doit être supérieure à 0");
        }
        if (dto.getNbVivants() != null && assoc.getEffectifInitial() != null
                && dto.getNbVivants() > assoc.getEffectifInitial()) {
            throw new IllegalArgumentException("Le nombre de vivants ne peut pas dépasser l'effectif initial ("
                    + assoc.getEffectifInitial() + ")");
        }
    }

    private void verifierAlerteRecolte(PeseeDTO dto, CycleBassinAssoc assoc) {
        if (dto.getPoidsMoyenGramme().compareTo(SEUIL_POIDS_RECOLTE) >= 0
                && dto.getTailleMoyenneMm().compareTo(SEUIL_TAILLE_RECOLTE) >= 0) {
            alerteService.creerAlerteDeRecolte(assoc);
        }
    }

    /**
     * Compare la biomasse de la nouvelle pesée avec celle de la pesée précédente.
     * Délègue la création d'alerte à {@link AlerteService#verifierVariationBiomasse}.
     *
     * La biomasse est lue depuis {@link SuiviHebdoBassin#getBiomasseCalculeeKg()}
     * qui est calculée automatiquement à partir du poids moyen et du nombre de vivants.
     */
    private void verifierAlerteBiomasse(CycleBassinAssoc assoc,
                                        Optional<SuiviHebdoBassin> peseePrecedenteOpt,
                                        SuiviHebdoBassin nouvellesPesee) {
        if (peseePrecedenteOpt.isEmpty()) {
            return; // première pesée : pas de comparaison possible
        }
        BigDecimal biomassePrec     = peseePrecedenteOpt.get().getBiomasseCalculeeKg();
        BigDecimal biomasseNouvelle = nouvellesPesee.getBiomasseCalculeeKg();
        alerteService.verifierVariationBiomasse(assoc, biomassePrec, biomasseNouvelle);
    }
}