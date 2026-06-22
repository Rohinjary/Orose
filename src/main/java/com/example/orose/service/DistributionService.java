package com.example.orose.service;

import com.example.orose.dto.DistributionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.example.orose.model.Bassin;
import com.example.orose.model.CreneauHoraire;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.DistributionNourriture;
import com.example.orose.model.EntreeStockAliment;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.*;
import com.example.orose.utils.RegleCreneau;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DistributionService {

    private final DistributionNourritureRepository distributionRepository;
    private final BassinRepository bassinRepository;
    private final StockAlimentService stockService;
    private final AlimentRepository alimentRepository;
    private final CreneauHoraireRepository creneauRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final StockRepository stockRepository;

    public DistributionService(DistributionNourritureRepository repo,
            BassinRepository bassinRepo,
            StockAlimentService stockService,
            AlimentRepository alimentRepository,
            CreneauHoraireRepository creneauRepository,
            UtilisateurRepository utilisateurRepository,
            CycleBassinAssocRepository cycleBassinAssocRepository,
            StockRepository stockRepository) { // Assurez-vous que StockRepository est bien le bon nom
        this.distributionRepository = repo;
        this.bassinRepository = bassinRepo;
        this.stockService = stockService;
        this.alimentRepository = alimentRepository;
        this.creneauRepository = creneauRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.stockRepository = stockRepository; // <--- C'EST CETTE LIGNE QUI MANQUAIT
    }

    @Transactional
    public DistributionNourriture validerDistribution(DistributionDTO dto) {
        // 1. Récupération du bassin
        Bassin bassinCible = bassinRepository.findById(dto.getIdBassin())
                .filter(b -> "ACTIF".equals(b.getStatutActuel().getCode()))
                .orElseThrow(() -> new RuntimeException("Bassin introuvable ou non ACTIF"));

        // 2. Récupération du cycle actif
        CycleBassinAssoc cycleAssoc = cycleBassinAssocRepository
                .findByBassinIdAndEstClotureFalse(bassinCible.getId().longValue())
                .orElseThrow(() -> new RuntimeException("Aucun cycle actif pour ce bassin"));

        // 3. Récupération de l'entrée stock
        EntreeStockAliment entreeStock = stockRepository
                .findFirstByAlimentIdOrderByDateReceptionDesc(dto.getIdAliment())
                .orElseThrow(() -> new RuntimeException("Aucun stock disponible pour cet aliment"));

        // CONVERSION : On convertit la quantité du DTO en BigDecimal pour le calcul
        BigDecimal qteDonnee = dto.getQuantiteDonneeKg();
        // 4. VÉRIFICATION : Stock suffisant avec BigDecimal
        if (entreeStock.getQuantiteRestanteKg().compareTo(qteDonnee) < 0) {
            throw new RuntimeException(
                    "Stock insuffisant ! Disponible : " + entreeStock.getQuantiteRestanteKg() + " KG");
        }

        // 5. MISE À JOUR DU STOCK (Calcul précis avec BigDecimal)
        entreeStock.setQuantiteRestanteKg(entreeStock.getQuantiteRestanteKg().subtract(qteDonnee));
        stockRepository.save(entreeStock);

        // 6. Construction de l'entité de distribution
        DistributionNourriture nouvelleDistribution = new DistributionNourriture();
        nouvelleDistribution.setCycleBassinAssoc(cycleAssoc);
        nouvelleDistribution.setEntreeAliment(entreeStock);

        // Automatisation créneau
        String libelle = RegleCreneau.determinerLibelle(LocalTime.now());
        CreneauHoraire creneau = creneauRepository.findByLibelle(libelle)
                .orElseThrow(() -> new RuntimeException("Créneau introuvable"));
        nouvelleDistribution.setCreneau(creneau);

        nouvelleDistribution.setDateDistribution(LocalDateTime.now().toLocalDate());
        nouvelleDistribution.setQuantiteDonneeKg(dto.getQuantiteDonneeKg());
        nouvelleDistribution.setQuantitePrevueKg(dto.getQuantiteDonneeKg());

        // Responsable
        Utilisateur agentResponsable = utilisateurRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new RuntimeException("Responsable non trouvé"));
        nouvelleDistribution.setResponsable(agentResponsable);

        nouvelleDistribution.setStatut("NOURRI");
        nouvelleDistribution.setEstValide(true);

        return distributionRepository.save(nouvelleDistribution);
    }
}