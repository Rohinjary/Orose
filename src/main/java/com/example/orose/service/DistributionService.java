package com.example.orose.service;

import com.example.orose.dto.DistributionDTO;
import com.example.orose.model.DistributionNourriture;
import com.example.orose.repository.DistributionNourritureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DistributionService {

    private final DistributionNourritureRepository distributionRepository;

    public DistributionService(DistributionNourritureRepository repo) {
        this.distributionRepository = repo;
    }

    @Transactional
    public DistributionNourriture validerDistribution(DistributionDTO dto) {
        // 1. Logique : Vérifier si le bassin est actif
        // 2. Logique : Appeler le StockAlimentService pour vérifier la dispo
        // 3. Logique : Enregistrer dans la table distribution_nourriture
        return new DistributionNourriture(); // Placeholder
    }
}