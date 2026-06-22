package com.example.orose.service;

import com.example.orose.dto.AlerteDTO;
import com.example.orose.dto.EntreeStockDTO;
import com.example.orose.model.*;
import com.example.orose.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.orose.dto.StockDetailDTO;

@Service
public class StockAlimentService {

    // Déclarations des dépendances
    private final EntreeStockAlimentRepository stockRepository;
    private final AlimentRepository alimentRepository;
    private final DistributionNourritureRepository distributionRepository;
    private final UtilisateurRepository utilisateurRepository;

    // Constructeur pour l'injection de dépendances
    public StockAlimentService(EntreeStockAlimentRepository stockRepository,
            AlimentRepository alimentRepository,
            DistributionNourritureRepository distributionRepository, UtilisateurRepository utilisateurRepository) {
        this.stockRepository = stockRepository;
        this.alimentRepository = alimentRepository;
        this.distributionRepository = distributionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // --- KPIs ---

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

    // ... dans votre méthode estimerAutonomieJours() ...

    public long estimerAutonomieJours() {
        BigDecimal stockTotal = getStockActuelTotal();

        // Calcul de la date limite en Java (propre et compatible)
        LocalDate dateLimite = LocalDate.now().minusDays(7);

        // Passage de la date au repository
        BigDecimal consoMoyenne = distributionRepository.getConsommationMoyenneQuotidienne(dateLimite);

        return (consoMoyenne == null || consoMoyenne.compareTo(BigDecimal.ZERO) == 0) ? 0
                : stockTotal.divide(consoMoyenne, java.math.RoundingMode.HALF_UP).longValue();
    }

    // --- RÈGLES MÉTIER ---

    public List<AlerteDTO> getAlertesStock() {
        List<AlerteDTO> alertes = new ArrayList<>();

        alimentRepository.findAll().forEach(a -> {
            BigDecimal stock = stockRepository.sumStockByAliment(a.getId().longValue());
            if (stock != null && stock.compareTo(a.getSeuilMinimumKg()) <= 0) {
                // Remplacement de 'false' (boolean) par 'ORANGE' (String)
                alertes.add(new AlerteDTO("Stock bas : " + a.getLibelle(), "ORANGE"));
            }
        });

        if (estimerAutonomieJours() < 7) {
            // Remplacement de 'true' (boolean) par 'ROUGE' (String)
            alertes.add(new AlerteDTO("Autonomie critique : < 7 jours", "ROUGE"));
        }

        return alertes;
    }

    @Transactional
    public void validerDistribution(DistributionNourriture dist) {
        // Règle : Bassin ACTIF obligatoire (Correction ici : getStatutActuel())
        if (dist.getCycle() == null || dist.getCycle().getBassin() == null ||
                !"ACTIF".equals(dist.getCycle().getBassin().getStatutActuel().getCode())) {
            throw new RuntimeException("Distribution impossible : bassin non ACTIF.");
        }

        // Règle : Une distribution par créneau (Correction ici : dateDistribution)
        if (distributionRepository.existsByCycleAndDateDistributionAndCreneau(
                dist.getCycle(), dist.getDateDistribution(), dist.getCreneau())) {
            throw new RuntimeException("Distribution déjà enregistrée pour ce créneau.");
        }

        // Règle : Stock suffisant
        if (dist.getQuantiteDonneeKg().compareTo(getStockActuelTotal()) > 0) {
            throw new RuntimeException("Stock aliment insuffisant.");
        }

        dist.setEstValide(true);
        distributionRepository.save(dist);
    }

    @Transactional
    public void enregistrerEntree(EntreeStockDTO dto) {
        // 1. Récupérer l'entité Aliment
        Aliment aliment = alimentRepository.findById(dto.getIdAliment().intValue())
                .orElseThrow(() -> new RuntimeException("Aliment introuvable"));

        // 2. Récupérer l'entité Responsable
        // On utilise l'ID envoyé par le nouveau menu déroulant du formulaire
        Utilisateur responsable = utilisateurRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new RuntimeException("Responsable introuvable"));
        // 3. Créer l'entité EntreeStockAliment
        EntreeStockAliment entree = new EntreeStockAliment();
        entree.setAliment(aliment);
        entree.setResponsable(responsable); // Assignation pour respecter la contrainte NOT NULL

        entree.setQuantiteKg(dto.getQuantiteKg());
        entree.setQuantiteRestanteKg(dto.getQuantiteKg());
        entree.setPrixUnitaireAr(dto.getPrixUnitaire());

        // Calcul du total
        entree.setPrixTotalAr(dto.getQuantiteKg().multiply(dto.getPrixUnitaire()));

        // Gestion des dates
        entree.setDateReception(dto.getDateReception());

        if (dto.getDateExpiration() != null) {
            entree.setDateExpiration(dto.getDateExpiration());
        } else {
            entree.setDateExpiration(dto.getDateReception().plusMonths(6));
        }

        // 4. Sauvegarder l'entité
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