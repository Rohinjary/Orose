package com.example.orose.service;

import com.example.orose.model.EntreeStockAliment;
import com.example.orose.repository.EntreeStockAlimentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class StockAlimentService {

    private final EntreeStockAlimentRepository stockRepository;

    public StockAlimentService(EntreeStockAlimentRepository repo) {
        this.stockRepository = repo;
    }

    @Transactional
    public void decrementerStock(BigDecimal quantiteDemandee) {
        // 1. Récupérer les stocks triés par date d'expiration (FIFO)
        List<EntreeStockAliment> stocks = stockRepository.findStocksDisponibles();

        BigDecimal aRetirer = quantiteDemandee;

        for (EntreeStockAliment entree : stocks) {
            if (aRetirer.compareTo(BigDecimal.ZERO) <= 0)
                break;

            BigDecimal disponible = entree.getQuantiteRestanteKg();

            if (disponible.compareTo(aRetirer) >= 0) {
                // Le lot suffit
                entree.setQuantiteRestanteKg(disponible.subtract(aRetirer));
                aRetirer = BigDecimal.ZERO;
            } else {
                // On vide le lot et on continue sur le suivant
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