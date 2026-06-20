package com.example.orose.service;

import com.example.orose.repository.StatutBassinRepository;
import com.example.orose.model.StatutBassin;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class StatutBassinService {
    private final StatutBassinRepository statutBassinRepository;

    public StatutBassinService(StatutBassinRepository statutBassinRepository) {
        this.statutBassinRepository = statutBassinRepository;
    }

    public StatutBassin getStatutBassinByCode(String code) {
        return statutBassinRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("StatutBassin introuvable"));
    }

    private static final Map<String, List<String>> TRANSITIONS_AUTORISEES = Map.of(
        "VIDE",         List.of("PREPARATION"),
        "PREPARATION",   List.of("ACTIF"),
        "ACTIF",         List.of("EN_TRAITEMENT", "RECOLTE"),
        "EN_TRAITEMENT", List.of("ACTIF"),
        "RECOLTE",       List.of("VIDE")
    );

    public boolean estTransitionAutorisee(String statutActuel, String nouveauStatut) {
        List<String> transitionsPossibles = TRANSITIONS_AUTORISEES.get(statutActuel);
        return transitionsPossibles != null && transitionsPossibles.contains(nouveauStatut);
    }

    public List<String> getTransitionsAutorisees(String statutActuelCode) {
        return TRANSITIONS_AUTORISEES.getOrDefault(statutActuelCode, List.of());
    }

    public void validerTransition(String statutActuelCode, String nouveauStatutCode) {
        List<String> autorisees = getTransitionsAutorisees(statutActuelCode);
        if (!autorisees.contains(nouveauStatutCode)) {
            throw new IllegalStateException(
                "Transition non autorisée : " + statutActuelCode + " → " + nouveauStatutCode);
        }
    }
}
//ssss