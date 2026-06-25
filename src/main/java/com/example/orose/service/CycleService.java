package com.example.orose.service;

import java.time.LocalDate;
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
        cycle.setDateDebut(dto.getDateDebut());
        cycle.setDateFinPrevue(dto.getDateFinPrevue());
        cycle.setEstCloture(false);

        Cycle savedCycle = cycleRepository.save(cycle);

        // Creer 1 CycleBassinAssoc par bassin
        for (Long idBassin : idBassins) {
            Bassin bassin = bassinRepository.findById(idBassin).get();

            CycleBassinAssoc assoc = new CycleBassinAssoc();
            assoc.setCycle(savedCycle);
            assoc.setBassin(bassin);
            assoc.setEffectifInitial(dto.getEffectifInitial().intValue());
            assoc.setCoutPostLarves(dto.getCoutPostLarves());
            assoc.setEstCloture(false);

            cycleBassinAssocRepository.save(assoc);

            // Passer le bassin en PREPARATION
            bassinService.changerStatutBassin(idBassin, "PREPARATION", "Demarrage cycle " + savedCycle.getCodeUniqueCycle(), 1L);
        }
    }

    private String genererCodeUniqueCycle() {
        long nbCycles = cycleRepository.count();
        return String.format("C%02d", nbCycles + 1);
    }

    public List<Cycle> getCyclesActif() {
        return cycleRepository.findByEstClotureFalse();
    }
<<<<<<< HEAD
=======

    public Cycle getCycleById(Long id) {
        return cycleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cycle introuvable : " + id));
    }

    public List<CycleBassinAssoc> getAssociationsByCycleId(Long cycleId) {
        return cycleBassinAssocRepository.findByCycleId(cycleId);
    }
>>>>>>> origin/dev
}
