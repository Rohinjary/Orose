package com.example.orose.service;

import com.example.orose.dto.DistributionDTO;

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
        // 1. Récupération du bassin avec vérification de statut
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

        // 4. AUTOMATISATION DU CRÉNEAU

        LocalTime heureActuelle = LocalTime.now();
        

        String libelle = RegleCreneau.determinerLibelle(heureActuelle);
        
        // Récupération de l'entité CreneauHoraire en base
        CreneauHoraire creneauDetecte = creneauRepository.findByLibelle(libelle)
                .orElseThrow(() -> new RuntimeException("Configuration créneau manquante pour : " + libelle));

        // 5. Construction de l'entité de distribution
        DistributionNourriture nouvelleDistribution = new DistributionNourriture();
        nouvelleDistribution.setCycleBassinAssoc(cycleAssoc);
        nouvelleDistribution.setEntreeAliment(entreeStock);
        nouvelleDistribution.setCreneau(creneauDetecte); 
        
        // On utilise LocalDateTime.now() pour avoir la précision temporelle complète
nouvelleDistribution.setDateDistribution(LocalDateTime.now().toLocalDate());        
        nouvelleDistribution.setQuantiteDonneeKg(dto.getQuantiteDonneeKg());
        nouvelleDistribution.setQuantitePrevueKg(dto.getQuantiteDonneeKg());

        // 6. Responsable
        Utilisateur agentResponsable = utilisateurRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new RuntimeException("Responsable non trouvé"));
        nouvelleDistribution.setResponsable(agentResponsable);

        nouvelleDistribution.setStatut("NOURRI");
        nouvelleDistribution.setEstValide(true);

        return distributionRepository.save(nouvelleDistribution);
    }
}