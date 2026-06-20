package com.example.orose.service;

import java.time.LocalDate;

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
    
    @Autowired
    public CycleBassinService(CycleBassinRepository cycleBassinRepository, BassinRepository bassinRepository, EspeceCrevetteRepository especeCrevetteRepository) {
        this.cycleBassinRepository = cycleBassinRepository;
        this.bassinRepository = bassinRepository;
        this.especeCrevetteRepository = especeCrevetteRepository;
    }

    public CycleBassin demarrerCycle(Long idBassin, CycleDemarrageDTO dto) {
        Bassin bassin = bassinRepository.findById(idBassin).orElse(null);

        String statutActuel = bassin.getStatutActuel().getCode();
        if (!"PREPARATION".equals(statutActuel)) {
            throw new IllegalStateException("Le bassin doit être en statut PREPARATION pour démarrer un cycle");
        }

        if (!cycleBassinRepository.existsByBassinIdAndEstClotureFalse(idBassin)) {
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
        cycle.setEstCloture(null);

        return cycleBassinRepository.save(cycle);
    }

    private String genererCodeUniqueCycle(Bassin bassin) {
        long nbCyclesTotal = cycleBassinRepository.countByBassinId(Long.valueOf(bassin.getId()));
        int annee = LocalDate.now().getYear();
        return String.format("%s-C%02d-%d", bassin.getCode(), nbCyclesTotal + 1, annee);
    }
}
