package com.example.orose.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.orose.model.Cycle;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.CycleRepository;
import com.example.orose.service.BassinService;

@Component
public class CycleScheduler {

    @Autowired private CycleRepository cycleRepository;
    @Autowired private CycleBassinAssocRepository cycleBassinAssocRepository;
    @Autowired private BassinService bassinService;

    // Exécuté au démarrage de l'application (rattrape les cycles manqués si l'app était éteinte)
    @EventListener(ApplicationReadyEvent.class)
    public void activerAuDemarrage() {
        activerCyclesEnAttente();
    }

    // Exécuté tous les jours à minuit
    @Scheduled(cron = "0 0 0 * * *")
    public void activerCyclesEnAttente() {
        LocalDate aujourdhui = LocalDate.now();

        List<Cycle> cycles = cycleRepository
            .findByEstClotureFalseAndDateDebutLessThanEqual(aujourdhui);

        for (Cycle cycle : cycles) {
            List<CycleBassinAssoc> assocs =
                cycleBassinAssocRepository.findByCycleId(cycle.getId().longValue());

            for (CycleBassinAssoc assoc : assocs) {
                if ("PREPARATION".equals(assoc.getBassin().getStatutActuel().getCode())) {
                    try {
                        bassinService.changerStatutBassin(
                            assoc.getBassin().getId().longValue(),
                            "ACTIF",
                            "Activation automatique — cycle " + cycle.getCodeUniqueCycle(),
                            1L
                        );
                    } catch (IllegalStateException e) {
                        System.err.println("Activation impossible pour bassin "
                            + assoc.getBassin().getCode() + " : " + e.getMessage());
                    }
                }
            }
        }
    }
}