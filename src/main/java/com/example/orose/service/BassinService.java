package com.example.orose.service;

import com.example.orose.dto.BassinDTO;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.orose.model.Bassin;
import com.example.orose.model.StatutBassin;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.StatutBassinRepository;
import com.example.orose.repository.CycleBassinRepository;

@Service
public class BassinService {    
    private BassinRepository bassinRepository; 
    private StatutBassinRepository statutBassinRepository;
    private CycleBassinRepository cycleBassinRepository;

    public Bassin creerBassin(BassinDTO dto) {
        if (bassinRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Le code du bassin doit être unique");
        }

        StatutBassin statutInitial = statutBassinRepository.findByCode("PREPARATION")
                .orElseThrow(() -> new IllegalArgumentException("Statut PREPARATION introuvable"));

        Bassin bassin = new Bassin();
        bassin.setCode(dto.getCode());
        bassin.setSurfaceM2(dto.getSurface_m2());
        bassin.setNotes(dto.getNotes());
        bassin.setProfondeurMetre(dto.getProfondeur_metre());
        bassin.setStatutActuel(statutInitial);

        return bassinRepository.save(bassin);
    }

    public void supprimerBassin(Long id) {
        Bassin bassin = bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));

        // Vérifier qu'aucun cycle n'est associé (historique de cycles)
        if (cycleBassinRepository.existsByBassinId(id)) {
            throw new IllegalStateException("Impossible de supprimer le bassin car il est associé à des cycles");
        }

        bassinRepository.delete(bassin);
    }

    public Bassin modifierBassin(Long id, BassinDTO dto) {
        Bassin bassin = bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));

        // Vérifier que le code est unique si modifié
        if (bassinRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Le code du bassin doit être unique");
        }

        bassin.setCode(dto.getCode());
        bassin.setSurfaceM2(dto.getSurface_m2());
        bassin.setProfondeurMetre(dto.getProfondeur_metre());
        bassin.setNotes(dto.getNotes());

        return bassinRepository.save(bassin);
    }

    public List<Bassin> listerBassins(){
        return bassinRepository.findAll();
    }

    public Bassin getBassinById(Long id) {
        return bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));
    }

}
