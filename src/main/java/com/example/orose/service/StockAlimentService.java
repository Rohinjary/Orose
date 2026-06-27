package com.example.orose.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orose.dto.EntreeStockDTO;
import com.example.orose.dto.StockDetailDTO;
import com.example.orose.model.Aliment;
import com.example.orose.model.EntreeStockAliment;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.AlimentRepository;
import com.example.orose.repository.EntreeStockAlimentRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.repository.nourrissage.DistributionNourritureRepository;

@Service
public class StockAlimentService {

    private final EntreeStockAlimentRepository stockRepository;
    private final AlimentRepository alimentRepository;
    private final DistributionNourritureRepository distributionRepository;
    private final UtilisateurRepository utilisateurRepository;

    public StockAlimentService(EntreeStockAlimentRepository stockRepository,
            AlimentRepository alimentRepository,
            DistributionNourritureRepository distributionRepository, UtilisateurRepository utilisateurRepository) {
        this.stockRepository = stockRepository;
        this.alimentRepository = alimentRepository;
        this.distributionRepository = distributionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public BigDecimal getStockActuelTotal() {
        BigDecimal total = stockRepository.sumQuantiteRestante();
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getValeurStockTotale() {
        BigDecimal total = stockRepository.sumValeurTotale();
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getVariationStockSemaine() {
        BigDecimal variation = stockRepository.sumEntreesDepuis(LocalDate.now().minusDays(7));
        return variation != null ? variation : BigDecimal.ZERO;
    }

    // public long estimerAutonomieJours() {
    // BigDecimal stockTotal = getStockActuelTotal();

    // // Calcul de la date limite en Java (propre et compatible)
    // LocalDate dateLimite = LocalDate.now().minusDays(7);

    // // Passage de la date au repository
    // BigDecimal consoMoyenne =
    // distributionRepository.getConsommationMoyenneQuotidienne(dateLimite);

    // return (consoMoyenne == null || consoMoyenne.compareTo(BigDecimal.ZERO) == 0)
    // ? 0
    // : stockTotal.divide(consoMoyenne,
    // java.math.RoundingMode.HALF_UP).longValue();
    // }

    // public List<AlerteDTO> getAlertesStock() {
    // List<AlerteDTO> alertes = new ArrayList<>();

    // alimentRepository.findAll().forEach(a -> {
    // BigDecimal stock = stockRepository.sumStockByAliment(a.getId().longValue());
    // if (stock != null && stock.compareTo(a.getSeuilMinimumKg()) <= 0) {
    // // Remplacement de 'false' (boolean) par 'ORANGE' (String)
    // alertes.add(new AlerteDTO("Stock bas : " + a.getLibelle(), "ORANGE"));
    // }
    // });

    // if (estimerAutonomieJours() < 7) {
    // // Remplacement de 'true' (boolean) par 'ROUGE' (String)
    // alertes.add(new AlerteDTO("Autonomie critique : < 7 jours", "ROUGE"));
    // }

    // return alertes;
    // }

    // @Transactional
    // public void validerDistribution(DistributionNourriture dist) {
    // // Accès au bassin via la table d'association
    // CycleBassinAssoc association = dist.getCycleBassinAssoc();

    // // Vérification : existence de l'association, du bassin et statut ACTIF
    // if (association == null || association.getBassin() == null ||
    // !"ACTIF".equals(association.getBassin().getStatutActuel().getCode())) {
    // throw new RuntimeException("Distribution impossible : bassin non ACTIF.");
    // }

    // // Vérification : une seule distribution par cycle/bassin, date et créneau
    // if
    // (distributionRepository.existsByCycleBassinAssocAndDateDistributionAndCreneau(
    // association, dist.getDateDistribution(), dist.getCreneau())) {
    // throw new RuntimeException("Distribution déjà enregistrée pour ce créneau.");
    // }

    // // Vérification : stock suffisant
    // if (dist.getQuantiteDonneeKg().compareTo(getStockActuelTotal()) > 0) {
    // throw new RuntimeException("Stock aliment insuffisant.");
    // }

    // dist.setEstValide(true);
    // distributionRepository.save(dist);
    // }

    @Transactional
    public void enregistrerEntree(EntreeStockDTO dto) {
        // 1. Récupérer les entités liées
        Aliment aliment = alimentRepository.findById(dto.getIdAliment())
                .orElseThrow(() -> new RuntimeException("Aliment introuvable"));

        Utilisateur responsable = utilisateurRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new RuntimeException("Responsable introuvable"));

        // 2. Créer l'entité
        EntreeStockAliment entree = new EntreeStockAliment();
        entree.setAliment(aliment);
        entree.setResponsable(responsable);

        // 3. Initialiser les quantités
        entree.setQuantiteKg(dto.getQuantiteKg());
        entree.setQuantiteRestanteKg(dto.getQuantiteKg()); // Indispensable pour éviter le null

        // 4. Prix unitaire uniquement
        entree.setPrixUnitaireAr(dto.getPrixUnitaire());

        /*
         * * MISE À JOUR CRITIQUE :
         * Ne PAS appeler setPrixTotalAr().
         * La base de données le calculera automatiquement.
         * En Java, on ne fait rien ici pour ce champ.
         */

        // 5. Gestion des dates
        entree.setDateReception(dto.getDateReception());
        entree.setDateExpiration(dto.getDateExpiration() != null
                ? dto.getDateExpiration()
                : dto.getDateReception().plusMonths(6));

        // 6. Sauvegarder
        stockRepository.save(entree);
    }

    public List<EntreeStockAliment> findAllStocks() {
        return stockRepository.findAll();
    }

    public List<StockDetailDTO> getDetailStocks() {
        List<StockDetailDTO> liste = new ArrayList<>();
        List<Aliment> aliments = alimentRepository.findAll();

        for (Aliment a : aliments) {
            BigDecimal stock = stockRepository.sumStockByAliment(a.getId().longValue());
            stock = (stock != null) ? stock : BigDecimal.ZERO;

            // Logique métier pour le statut
            String statut = (stock.compareTo(BigDecimal.ZERO) == 0) ? "Rupture"
                    : (stock.compareTo(a.getSeuilMinimumKg()) <= 0 ? "À Commander" : "En Stock");
            String css = (statut.equals("Rupture")) ? "danger" : (statut.equals("À Commander")) ? "warning" : "success";

            liste.add(new StockDetailDTO(a.getLibelle(), stock, 0, statut, css)); // 15 = autonomie fictive
        }
        return liste;
    }
}