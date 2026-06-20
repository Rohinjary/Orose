package com.example.orose.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.orose.repository.CycleBassinRepository;

import jakarta.persistence.EntityNotFoundException;

import com.example.orose.dto.CycleDemarrageDTO;
import com.example.orose.model.CycleBassin;
import com.example.orose.repository.BassinRepository;
import com.example.orose.model.Bassin;
import com.example.orose.model.EspeceCrevette;
import com.example.orose.repository.EspeceCrevetteRepository;

@Service
public class CycleBassinService {
    private CycleBassinRepository cycleBassinRepository;
    private BassinRepository bassinRepository;
    private EspeceCrevetteRepository especeCrevetteRepository;
    private final BassinService bassinService;
    
    @Autowired
    public CycleBassinService(CycleBassinRepository cycleBassinRepository, BassinRepository bassinRepository, EspeceCrevetteRepository especeCrevetteRepository, BassinService bassinService) {
        this.cycleBassinRepository = cycleBassinRepository;
        this.bassinRepository = bassinRepository;
        this.especeCrevetteRepository = especeCrevetteRepository;
        this.bassinService = bassinService;
    }

    public CycleBassin demarrerCycle(Long idBassin, CycleDemarrageDTO dto) {
        Bassin bassin = bassinRepository.findById(idBassin).orElse(null);

        String statutActuel = bassin.getStatutActuel().getCode();
        if (!"PREPARATION".equals(statutActuel)) {
            throw new IllegalStateException("Le bassin doit être en statut PREPARATION pour démarrer un cycle");
        }

        if (cycleBassinRepository.existsByBassinIdAndEstClotureFalse(idBassin)) {
            throw new IllegalStateException("Un cycle est déjà en cours pour ce bassin");
        }

        EspeceCrevette espece = especeCrevetteRepository.findById((long) dto.getIdEspece())
                .orElseThrow(() -> new EntityNotFoundException("Espèce introuvable : " + dto.getIdEspece()));


        CycleBassin cycle = new CycleBassin();
        cycle.setCodeUniqueCycle(genererCodeUniqueCycle(bassin));
        cycle.setBassin(bassin);
        cycle.setEspece(espece);
        cycle.setEffectifInitial(dto.getEffectifInitial().intValue());
        cycle.setCoutPostLarves(dto.getCoutPostLarves());
        cycle.setDateDebut(dto.getDateDebut());
        cycle.setDateFinPrevue(dto.getDateFinPrevue());
        cycle.setDateFinReelle(null);
        cycle.setPoidsMoyenActuel(null);
        cycle.setEstCloture(false);

        CycleBassin savedCycle = cycleBassinRepository.save(cycle);

        bassinService.changerStatutBassin(idBassin, "ACTIF", "Cycle demarre automatiquement", 1L);

        return savedCycle;
    }

    private String genererCodeUniqueCycle(Bassin bassin) {
        long nbCyclesTotal = cycleBassinRepository.countByBassinId(Long.valueOf(bassin.getId()));
        int annee = LocalDate.now().getYear();
        return String.format("%s-C%02d-%d", bassin.getCode(), nbCyclesTotal + 1, annee);
    }

    public List<CycleBassin> getCyclesActif() {
        return cycleBassinRepository.findByEstClotureFalse();
    }

    public void demarrerCycle(List<Long> idBassins, CycleDemarrageDTO dto) {
        if (idBassins.size() != 3) {
            throw new IllegalStateException("Exactement 3 bassins doivent etre selectionnes");
        }

        EspeceCrevette espece = especeCrevetteRepository.findById((long) dto.getIdEspece())
                .orElseThrow(() -> new EntityNotFoundException("Espece introuvable"));

        for (Long idBassin : idBassins) {
            Bassin bassin = bassinRepository.findById(idBassin)
                    .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable : " + idBassin));

            if (!"VIDE".equals(bassin.getStatutActuel().getCode())) {
                throw new IllegalStateException("Le bassin " + bassin.getCode() + " n'est pas VIDE");
            }

            CycleBassin cycle = new CycleBassin();
            cycle.setCodeUniqueCycle(genererCodeUniqueCycle(bassin));
            cycle.setBassin(bassin);
            cycle.setEspece(espece);
            cycle.setEffectifInitial(dto.getEffectifInitial().intValue());
            cycle.setCoutPostLarves(dto.getCoutPostLarves());
            cycle.setDateDebut(dto.getDateDebut());
            cycle.setDateFinPrevue(dto.getDateFinPrevue());
            cycle.setDateFinReelle(null);
            cycle.setPoidsMoyenActuel(null);
            cycle.setEstCloture(false);

            cycleBassinRepository.save(cycle);

            // Passer en PREPARATION automatiquement
            bassinService.changerStatutBassin(idBassin, "PREPARATION", "Cycle demarre", 1L);
        }
    }
}
