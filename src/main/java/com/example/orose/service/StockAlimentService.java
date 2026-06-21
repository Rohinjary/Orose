package com.example.orose.service;

import com.example.orose.model.EntreeStockAliment;
import com.example.orose.model.Aliment;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.EntreeStockAlimentRepository;
import com.example.orose.repository.AlimentRepository; // Assurez-vous d'avoir ce repo
import com.example.orose.dto.EntreeStockAlimentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class StockAlimentService {

    private final EntreeStockAlimentRepository stockRepository;
    private final AlimentRepository alimentRepository;

    public StockAlimentService(EntreeStockAlimentRepository repo, AlimentRepository alimentRepo) {
        this.stockRepository = repo;
        this.alimentRepository = alimentRepo;
    }

    @Transactional
    public EntreeStockAliment enregistrerEntreeAliment(EntreeStockAlimentDTO dto, Utilisateur responsable) {
        // 1. Récupération des entités liées
        Aliment aliment = alimentRepository.findById(Math.toIntExact(dto.getIdAliment()))
            .orElseThrow(() -> new RuntimeException("Aliment non trouvé"));
        // 2. Mapping complet vers l'entité
        EntreeStockAliment entree = new EntreeStockAliment();
        entree.setAliment(aliment);
        entree.setResponsable(responsable);
        
        entree.setQuantiteKg(dto.getQuantiteKg());
        entree.setQuantiteRestanteKg(dto.getQuantiteKg()); // Initialisation stock
        
        entree.setPrixUnitaireAr(dto.getPrixUnitaire());
        entree.setPrixTotalAr(dto.getPrixTotalAr()); // Reçu du formulaire JS
        
        entree.setDateReception(LocalDate.now()); // Date du jour par défaut
        entree.setDateExpiration(dto.getDateExpiration());
        
        return stockRepository.save(entree);
    }

    @Transactional
    public void decrementerStock(BigDecimal quantiteDemandee) {
        List<EntreeStockAliment> stocks = stockRepository.findStocksDisponibles(); 
        BigDecimal aRetirer = quantiteDemandee;

        for (EntreeStockAliment entree : stocks) {
            if (aRetirer.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal disponible = entree.getQuantiteRestanteKg();

            if (disponible.compareTo(aRetirer) >= 0) {
                entree.setQuantiteRestanteKg(disponible.subtract(aRetirer));
                aRetirer = BigDecimal.ZERO;
            } else {
                aRetirer = aRetirer.subtract(disponible);
                entree.setQuantiteRestanteKg(BigDecimal.ZERO);
            }
            stockRepository.save(entree);
        }

        if (aRetirer.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Stock insuffisant pour cette distribution !");
        }
    }
}