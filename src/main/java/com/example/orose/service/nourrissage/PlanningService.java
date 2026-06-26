package com.example.orose.service.nourrissage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.orose.dto.nourrissage.PlanningBassinDTO;
import com.example.orose.dto.nourrissage.PlanningJourProjection;

import org.springframework.stereotype.Service;

import com.example.orose.repository.nourrissage.PlanningRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final PlanningRepository planningRepository;

    public List<PlanningJourProjection> obtenirPlanningDuJour(Integer idUtilisateur) {
        return planningRepository.obtenirOuCreerPlanningDuJour(idUtilisateur);
    }

    public List<PlanningBassinDTO> construireTableau(Integer idUtilisateur) {

        List<PlanningJourProjection> lignes = planningRepository.obtenirOuCreerPlanningDuJour(idUtilisateur);

        Map<String, PlanningBassinDTO> map = new LinkedHashMap<>();

        for (PlanningJourProjection p : lignes) {

            PlanningBassinDTO dto = map.computeIfAbsent(
                    p.getCodeBassin(),
                    k -> {
                        PlanningBassinDTO b = new PlanningBassinDTO();
                        b.setCodeBassin(p.getCodeBassin());
                        b.setNoteBassin(p.getNoteBassin());
                        return b;
                    });

            switch (p.getCreneauLibelle()) {
                case "MATIN" -> dto.setMatin(p);
                case "MIDI" -> dto.setMidi(p);
                case "SOIR" -> dto.setSoir(p);
                case "NUIT" -> dto.setNuit(p);
            }
        }

        return new ArrayList<>(map.values());
    }
}