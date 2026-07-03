package com.example.orose.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

import com.example.orose.dto.CycleDemarrageDTO;
import com.example.orose.model.Cycle;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.Bassin;
import com.example.orose.model.EspeceCrevette;
import com.example.orose.repository.CycleRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.EspeceCrevetteRepository;

@Service
public class CycleService {

    private final CycleRepository cycleRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final BassinRepository bassinRepository;
    private final EspeceCrevetteRepository especeCrevetteRepository;
    private final BassinService bassinService;

    @Autowired
    public CycleService(CycleRepository cycleRepository,
                               CycleBassinAssocRepository cycleBassinAssocRepository,
                               BassinRepository bassinRepository,
                               EspeceCrevetteRepository especeCrevetteRepository,
                               BassinService bassinService) {
        this.cycleRepository = cycleRepository;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.bassinRepository = bassinRepository;
        this.especeCrevetteRepository = especeCrevetteRepository;
        this.bassinService = bassinService;
    }

    public void demarrerCycle(List<Long> idBassins, CycleDemarrageDTO dto) {
        if (idBassins.size() != 3) {
            throw new IllegalStateException("Exactement 3 bassins doivent etre selectionnes");
        }

        // Verifier que tous les bassins sont VIDE avant de commencer
        for (Long idBassin : idBassins) {
            Bassin bassin = bassinRepository.findById(idBassin)
                    .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable : " + idBassin));

            if (!"VIDE".equals(bassin.getStatutActuel().getCode())) {
                throw new IllegalStateException("Le bassin " + bassin.getCode() + " n'est pas VIDE");
            }

            if (cycleBassinAssocRepository.existsByBassinIdAndEstClotureFalse(idBassin)) {
                throw new IllegalStateException("Le bassin " + bassin.getCode() + " a deja un cycle en cours");
            }
        }

        EspeceCrevette espece = especeCrevetteRepository.findById((long) dto.getIdEspece())
                .orElseThrow(() -> new EntityNotFoundException("Espece introuvable"));

        // Creer le cycle global (1 seul cycle pour les 3 bassins)
        Cycle cycle = new Cycle();
        cycle.setCodeUniqueCycle(genererCodeUniqueCycle());
        cycle.setEspece(espece);
        cycle.setTechnicien(null);
        cycle.setDateDebut(dto.getDateDebut());
        cycle.setDateFinPrevue(dto.getDateFinPrevue());
        cycle.setEstCloture(false);

        Cycle savedCycle = cycleRepository.save(cycle);

        // Calculer la surface totale des 3 bassins
        BigDecimal surfaceTotale = idBassins.stream()
            .map(id -> bassinRepository.findById(id).get().getSurfaceM2())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal densiteM2 = dto.getEffectifInitial()
            .divide(surfaceTotale, 2, RoundingMode.HALF_UP);

        // Si dateDebut <= aujourd'hui : ACTIF immédiatement, sinon PREPARATION (le scheduler activera plus tard)
        String statutCible = !dto.getDateDebut().isAfter(LocalDate.now()) ? "ACTIF" : "PREPARATION";
        String motif = "ACTIF".equals(statutCible)
            ? "Demarrage immediat — cycle " + savedCycle.getCodeUniqueCycle()
            : "Demarrage cycle " + savedCycle.getCodeUniqueCycle();

        // Creer 1 CycleBassinAssoc par bassin
        int semaineActuelle = calculerSemaineActuelle(dto.getDateDebut());
        
        for (Long idBassin : idBassins) {
            Bassin bassin = bassinRepository.findById(idBassin).get();

            CycleBassinAssoc assoc = new CycleBassinAssoc();
            assoc.setCycle(savedCycle);
            assoc.setBassin(bassin);
            assoc.setDensiteM2(densiteM2);
            assoc.setSemaineActuelle(semaineActuelle);
            assoc.setEffectifInitial(dto.getEffectifInitial().intValue());
            assoc.setCoutPostLarves(dto.getCoutPostLarves());
            assoc.setEstCloture(false);

            cycleBassinAssocRepository.save(assoc);

            bassinService.changerStatutBassin(idBassin, statutCible, motif, 1L);
        }
    }

    public int calculerSemaineActuelle(LocalDate dateDebut) {
        long joursEcoules = ChronoUnit.DAYS.between(dateDebut, LocalDate.now());
        if (joursEcoules < 0) {
            return 0; // cycle pas encore démarré
        }
        return (int) (joursEcoules / 7); 
    }

    private String genererCodeUniqueCycle() {
        long nbCycles = cycleRepository.count();
        return String.format("C%02d", nbCycles + 1);
    }

    public List<Cycle> getCyclesActif() {
        return cycleRepository.findByEstClotureFalse();
    }

    public Cycle getCycleById(Long id) {
        return cycleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cycle introuvable : " + id));
    }

    public List<CycleBassinAssoc> getAssociationsByCycleId(Long cycleId) {
        return cycleBassinAssocRepository.findByCycleId(cycleId);
    }
}