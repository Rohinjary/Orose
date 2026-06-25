package com.example.orose.service;

import com.example.orose.dto.PeseeDTO;
import com.example.orose.model.Cycle;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.SuiviHebdoBassin;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.SuiviHebdoBassinRepository;
import com.example.orose.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PeseeService {

    private static final BigDecimal SEUIL_POIDS_RECOLTE = new BigDecimal("15");
    private static final BigDecimal SEUIL_TAILLE_RECOLTE = new BigDecimal("110");

    private final SuiviHebdoBassinRepository suiviHebdoBassinRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AlerteService alerteService;

    public PeseeService(SuiviHebdoBassinRepository suiviHebdoBassinRepository,
                        CycleBassinAssocRepository cycleBassinAssocRepository,
                        UtilisateurRepository utilisateurRepository,
                        AlerteService alerteService) {
        this.suiviHebdoBassinRepository = suiviHebdoBassinRepository;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.alerteService = alerteService;
    }

    public SuiviHebdoBassin enregistrerPesee(PeseeDTO dto) {
        CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(dto.getIdCycleBassinAssoc().longValue())
                .orElseThrow(() -> new EntityNotFoundException("Association cycle-bassin introuvable"));

        if (Boolean.TRUE.equals(assoc.getEstCloture())) {
            throw new IllegalStateException("Le cycle-bassin est clôturé, impossible d'enregistrer une pesée");
        }

        if (assoc.getBassin().getStatutActuel() == null
                || !"ACTIF".equals(assoc.getBassin().getStatutActuel().getCode())) {
            throw new IllegalStateException("Le bassin doit être en statut ACTIF pour enregistrer une pesée");
        }

        if (dto.getPoidsMoyenGramme() == null || dto.getPoidsMoyenGramme().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le poids moyen doit être supérieur à 0");
        }
        if (dto.getTailleMoyenneMm() == null || dto.getTailleMoyenneMm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La taille moyenne doit être supérieure à 0");
        }
        if (dto.getNbVivants() != null && dto.getNbVivants() > assoc.getEffectifInitial()) {
            throw new IllegalArgumentException("Le nombre de vivants ne peut pas dépasser l'effectif initial ("
                    + assoc.getEffectifInitial() + ")");
        }

        Cycle cycle = assoc.getCycle();
        int semaineActuelle = (int) ChronoUnit.WEEKS.between(cycle.getDateDebut(), dto.getDateSuivi()) + 1;

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

        assoc.setPoidsMoyenActuel(dto.getPoidsMoyenGramme());
        assoc.setSemaineActuelle(semaineActuelle);
        cycleBassinAssocRepository.save(assoc);

        if (dto.getPoidsMoyenGramme().compareTo(SEUIL_POIDS_RECOLTE) >= 0
                && dto.getTailleMoyenneMm().compareTo(SEUIL_TAILLE_RECOLTE) >= 0) {
            alerteService.creerAlerteDeRecolte(assoc);
        }

        return saved;
    }

    public SuiviHebdoBassin modifierPesee(Long id, PeseeDTO dto) {
        SuiviHebdoBassin pesee = suiviHebdoBassinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pesée introuvable : " + id));

        CycleBassinAssoc assoc = pesee.getCycleBassinAssoc();
        if (Boolean.TRUE.equals(assoc.getEstCloture())) {
            throw new IllegalStateException("Le cycle-bassin est clôturé, impossible de modifier la pesée");
        }

        if (dto.getPoidsMoyenGramme() == null || dto.getPoidsMoyenGramme().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le poids moyen doit être supérieur à 0");
        }
        if (dto.getTailleMoyenneMm() == null || dto.getTailleMoyenneMm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La taille moyenne doit être supérieure à 0");
        }
        if (dto.getNbVivants() != null && dto.getNbVivants() > assoc.getEffectifInitial()) {
            throw new IllegalArgumentException("Le nombre de vivants ne peut pas dépasser l'effectif initial");
        }

        Cycle cycle = assoc.getCycle();
        int semaineActuelle = (int) ChronoUnit.WEEKS.between(cycle.getDateDebut(), dto.getDateSuivi()) + 1;

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

        Optional<SuiviHebdoBassin> derniere = suiviHebdoBassinRepository
                .findTopByCycleBassinAssocIdOrderByDateSuiviDesc(assoc.getId());
        if (derniere.isPresent() && derniere.get().getId().equals(saved.getId())) {
            assoc.setPoidsMoyenActuel(dto.getPoidsMoyenGramme());
            assoc.setSemaineActuelle(semaineActuelle);
            cycleBassinAssocRepository.save(assoc);

            if (dto.getPoidsMoyenGramme().compareTo(SEUIL_POIDS_RECOLTE) >= 0
                    && dto.getTailleMoyenneMm().compareTo(SEUIL_TAILLE_RECOLTE) >= 0) {
                alerteService.creerAlerteDeRecolte(assoc);
            }
        }

        return saved;
    }

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
     * Archivage (soft delete) — la colonne est_archive n'existe pas encore en base.
     * La suppression physique est interdite ; cette méthode est un no-op documenté.
     */
    public void archiverPesee(Long id) {
        suiviHebdoBassinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pesée introuvable : " + id));
        throw new UnsupportedOperationException(
                "Archivage non disponible : la colonne est_archive n'existe pas dans la table suivi_hebdo_bassin. "
                        + "Ajouter la colonne en base et dans l'entité SuiviHebdoBassin pour activer cette fonctionnalité.");
    }
}
