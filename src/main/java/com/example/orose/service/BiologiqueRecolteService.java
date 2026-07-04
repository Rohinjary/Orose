package com.example.orose.service;

import com.example.orose.dto.DeclarRecolteFormDTO;
import com.example.orose.model.*;
import com.example.orose.repository.*;
import com.example.orose.repository.stock.LotCrevetteRepository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Service pour gérer la déclaration et la validation de récolte
 */
@Service
@RequiredArgsConstructor
public class BiologiqueRecolteService {

    private final RecolteDeclarationRepository recolteRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final BassinRepository bassinRepository;
    private final CycleRepository cycleRepository;
    private final LotCrevetteRepository lotCrevetteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BassinService bassinService;
    private final SuiviHebdoBassinRepository suiviHebdoBassinRepository;

    private static final BigDecimal POIDS_INITIAL_G = new BigDecimal("20");  // 20g par post-larve

    /**
     * Prépare le DTO pour le formulaire de déclaration de récolte
     */
    public DeclarRecolteFormDTO prepareFormulairDeclaration(Integer idCycleBassinAssoc) {
        CycleBassinAssoc cba = cycleBassinAssocRepository.findById(idCycleBassinAssoc.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Cycle-Bassin introuvable"));

        Bassin bassin = cba.getBassin();
        Cycle cycle = cba.getCycle();

        // Calcul de la récolte estimée
        BigDecimal recolteEstimee = BigDecimal.valueOf(cba.getEffectifInitial())
                .multiply(POIDS_INITIAL_G)
                .divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);  // Convertir de g à kg

        return DeclarRecolteFormDTO.builder()
                .idCycleBassinAssoc(idCycleBassinAssoc)
                .codeBassin(bassin.getCode())
                .codeUniqueCycle(cycle.getCodeUniqueCycle())
                .semaineActuelle(cba.getSemaineActuelle())
                .effectifInitial(cba.getEffectifInitial())
                .recolteEstimeeKg(recolteEstimee)
                .build();
    }

    /**
     * Valide et enregistre la déclaration de récolte
     * Crée automatiquement une entrée dans le stock de crevettes
     */
    @Transactional
    public void validerDeclarationRecolte(Integer idCycleBassinAssoc, BigDecimal recolteReelleKg, Utilisateur responsable) {

        // Validation
        if (recolteReelleKg == null || recolteReelleKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La quantité réelle doit être supérieure à 0");
        }

        CycleBassinAssoc cba = cycleBassinAssocRepository.findById(idCycleBassinAssoc.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Cycle-Bassin introuvable"));

        // Vérifier qu'il n'y a pas déjà une déclaration
        if (recolteRepository.existsByIdCycleBassinAssocId(idCycleBassinAssoc)) {
            throw new IllegalStateException("Une déclaration de récolte existe déjà pour ce cycle-bassin");
        }

        // Calcul de la récolte estimée
        BigDecimal recolteEstimee = BigDecimal.valueOf(cba.getEffectifInitial())
                .multiply(POIDS_INITIAL_G)
                .divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);

        // Créer l'enregistrement de récolte
        RecolteDeclaration recolte = RecolteDeclaration.builder()
                .cycleBassinAssoc(cba)
                .recolteEstimeeKg(recolteEstimee)
                .recolteReelleKg(recolteReelleKg)
                .dateDeclaration(LocalDate.now())
                .responsable(responsable)
                .build();

        RecolteDeclaration recolteEnregistree = recolteRepository.save(recolte);

        // Créer l'entrée stock crevette (lot_crevette)
        creerLotCrevette(cba, recolteEnregistree, responsable);

        // Marquer le cycle-bassin comme clôturé
        cba.setEstCloture(true);
        cba.setDateFinReelle(LocalDate.now());
        cycleBassinAssocRepository.save(cba);

        // Vider le bassin après validation de la récolte
        bassinService.changerStatutBassin(
                cba.getBassin().getId().longValue(),
                "VIDE",
                "Récolte validée — bassin vidé après création du lot",
                responsable.getId().longValue()
        );
    }

    /**
     * Crée un lot de crevettes après validation de récolte
     */
    private void creerLotCrevette(CycleBassinAssoc cba, RecolteDeclaration recolte, Utilisateur responsable) {
        // Générer le numéro de lot unique
        String numeroLot = generateNumeroLot(cba.getBassin().getCode());

        BigDecimal poidsMoyenFinalG = calculerPoidsMoyenFinalG(cba);
        BigDecimal tailleMoyenneFinaleMm = calculerTailleMoyenneFinaleMm(cba);

        LotCrevette lot = LotCrevette.builder()
                .numeroLotUnique(numeroLot)
                .recolteDeclaration(recolte)
                .poidsMoyenFinalG(poidsMoyenFinalG)
                .tailleMoyenneFinaleMm(tailleMoyenneFinaleMm)
                .dateRecolte(LocalDate.now())
                .responsable(responsable)
                .build();

        lotCrevetteRepository.save(lot);
    }

    private BigDecimal calculerPoidsMoyenFinalG(CycleBassinAssoc cba) {
        return dernierSuivi(cba)
                .map(SuiviHebdoBassin::getPoidsMoyenGramme)
                .orElseGet(() -> {
                    Integer effectifInitial = cba.getEffectifInitial();
                    if (effectifInitial == null || effectifInitial <= 0) {
                        return BigDecimal.ZERO;
                    }
                    return BigDecimal.ZERO;
                });
    }

    private BigDecimal calculerTailleMoyenneFinaleMm(CycleBassinAssoc cba) {
        return dernierSuivi(cba)
                .map(SuiviHebdoBassin::getTailleMoyenneMm)
                .orElse(BigDecimal.ZERO);
    }

    private Optional<SuiviHebdoBassin> dernierSuivi(CycleBassinAssoc cba) {
        if (cba == null || cba.getId() == null) {
            return Optional.empty();
        }
        return suiviHebdoBassinRepository.findTopByCycleBassinAssocIdOrderByDateSuiviDesc(cba.getId());
    }

    /**
     * Génère un numéro de lot unique
     */
    private String generateNumeroLot(String codeBassin) {
        long count = lotCrevetteRepository.count();
        return String.format("LOT-%s-%d-%d", codeBassin, LocalDate.now().getYear(), count + 1);
    }

    /**
     * Récupère les informations détaillées de récolte
     */
    public DeclarRecolteFormDTO obtenirDetailRecolte(Integer idCycleBassinAssoc) {
        DeclarRecolteFormDTO form = prepareFormulairDeclaration(idCycleBassinAssoc);

        // Chercher la déclaration existante
        recolteRepository.findByIdCycleBassinAssocIdOrderByIdDesc(idCycleBassinAssoc)
                .ifPresent(recolte -> {
                    form.setRecolteReelleKg(recolte.getRecolteReelleKg());
                    form.setPerteKg(recolte.getPerteKg());
                    form.setTauxSurviePercent(recolte.getTauxSurviePercent());
                });

        return form;
    }
}
