package com.example.orose.service.nourrissage;


import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.orose.dto.nourrissage.DashboardDTO;
import com.example.orose.dto.nourrissage.DashboardStatProjection;
import com.example.orose.dto.nourrissage.PlanningJourProjection;
import com.example.orose.repository.EntreeStockRepository;
import com.example.orose.repository.nourrissage.DistributionNourritureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DistributionNourritureRepository nourrissageRepository;
    private final EntreeStockRepository stockRepository;

    public DashboardDTO chargerDashboard(Integer idUtilisateur) {

        DashboardDTO dto = new DashboardDTO();

        PlanningJourProjection prochain =
                nourrissageRepository.findProchainRepas(idUtilisateur);

        if (prochain != null) {
            dto.setProchainBassin(prochain.getCodeBassin());
            dto.setProchaineHeure(prochain.getHeurePrevue());
        }

        dto.setNbEnAttente(
                nourrissageRepository.countRepasEnAttente(idUtilisateur)
        );

        DashboardStatProjection stat =
                nourrissageRepository.getStatistiquesPlanning(idUtilisateur);

        long total =
                stat.getTotal() == null ? 0 : stat.getTotal();

        long distribues =
                stat.getDistribues() == null ? 0 : stat.getDistribues();

        dto.setTotalRepas(total);
        dto.setRepasDistribues(distribues);

        double pourcentage = total == 0
                ? 0
                : (distribues * 100.0 / total);

        dto.setPourcentageDistribution(pourcentage);

        Double stock =
                stockRepository.getStockDisponible();

        Double conso =
                stockRepository.getConsommationMoyenneJour();

        dto.setStockDisponible(
                BigDecimal.valueOf(stock)
        );

        int autonomie = 0;

        if (conso != null && conso > 0) {
            autonomie = (int) Math.floor(stock / conso);
        }

        dto.setAutonomieJour(autonomie);

        dto.setStockCritique(autonomie < 7);

        return dto;
    }
}
