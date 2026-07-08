package com.example.orose.service;

import com.example.orose.dto.IncidentDTO;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.IncidentSanitaire;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.IncidentSanitaireRepository;
import com.example.orose.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class IncidentService {

    @Autowired
    private IncidentSanitaireRepository incidentRepository;
    @Autowired
    private CycleBassinAssocRepository cycleBassinAssocRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private BassinService bassinService;

    @Transactional
    public IncidentSanitaire declarerIncident(IncidentDTO dto) {
        CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(dto.getIdCycleBassinAssoc().longValue())
                .orElseThrow(() -> new RuntimeException("Association cycle-bassin non trouvée"));
        Utilisateur responsable = utilisateurRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (assoc.getBassin() == null || assoc.getBassin().getStatutActuel() == null) {
            throw new IllegalStateException("Le bassin sélectionné n'a pas de statut valide.");
        }
        String statut = assoc.getBassin().getStatutActuel().getCode();
        if (!"ACTIF".equalsIgnoreCase(statut) && !"EN_TRAITEMENT".equalsIgnoreCase(statut)) {
            throw new IllegalStateException("Le bassin sélectionné doit être ACTIF ou EN_TRAITEMENT.");
        }

        String gravite = normaliserNiveauGravite(dto.getNiveauGravite());

        IncidentSanitaire incident = new IncidentSanitaire();
        incident.setCycleBassinAssoc(assoc);
        incident.setDateDetection(dto.getDateDetection());
        incident.setTypeIncident(dto.getTypeIncident());
        incident.setDescription(dto.getDescription());
        incident.setNiveauGravite(gravite);
        incident.setResponsable(responsable);
        incident.setEstResolu(false);
        incident.setCreatedAt(LocalDateTime.now());

        IncidentSanitaire saved = incidentRepository.save(incident);

        // Quand un incident est déclaré, le bassin concerné passe en EN_TRAITEMENT.
        Long bassinId = assoc.getBassin().getId().longValue();
        bassinService.changerStatutBassin(
                bassinId,
                "EN_TRAITEMENT",
                "Incident déclaré — traitement sanitaire",
                1L
        );

        return saved;
    }

    @Transactional
    public IncidentSanitaire modifierIncident(Integer id, IncidentDTO dto) {
        IncidentSanitaire incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));
        incident.setDescription(dto.getDescription());
        incident.setNiveauGravite(normaliserNiveauGravite(dto.getNiveauGravite()));
        incident.setTypeIncident(dto.getTypeIncident());
        return incidentRepository.save(incident);
    }

    private String normaliserNiveauGravite(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return "FAIBLE";
        }
        String normalisee = valeur.trim().toUpperCase(Locale.ROOT);
        return switch (normalisee) {
            case "CRITIQUE", "MODERE", "MODÉRÉ", "FAIBLE" -> normalisee.replace("MODÉRÉ", "MODERE");
            default -> "FAIBLE";
        };
    }

    public List<IncidentSanitaire> getIncidentsByCycleBassinAssoc(Integer idCycleBassinAssoc) {
        return incidentRepository.findByCycleBassinAssocId(idCycleBassinAssoc);
    }

    public IncidentSanitaire getIncidentById(Integer id) {
        return incidentRepository.findById(id).orElse(null);
    }

    @Transactional
    public IncidentSanitaire resoudreIncident(Integer id) {
        IncidentSanitaire incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));
        incident.setEstResolu(true);
        return incidentRepository.save(incident);
    }
}
