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

@Service
public class IncidentService {

    @Autowired
    private IncidentSanitaireRepository incidentRepository;
    @Autowired
    private CycleBassinAssocRepository cycleBassinAssocRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Transactional
    public IncidentSanitaire declarerIncident(IncidentDTO dto) {
        CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(dto.getIdCycleBassinAssoc().longValue())
                .orElseThrow(() -> new RuntimeException("Association cycle-bassin non trouvée"));
        Utilisateur responsable = utilisateurRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        IncidentSanitaire incident = new IncidentSanitaire();
        incident.setCycleBassinAssoc(assoc);
        incident.setDateDetection(dto.getDateDetection());
        incident.setTypeIncident(dto.getTypeIncident());
        incident.setDescription(dto.getDescription());
        incident.setNiveauGravite(dto.getNiveauGravite());
        incident.setResponsable(responsable);
        incident.setEstResolu(false);
        incident.setCreatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    @Transactional
    public IncidentSanitaire modifierIncident(Integer id, IncidentDTO dto) {
        IncidentSanitaire incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));
        incident.setDescription(dto.getDescription());
        incident.setNiveauGravite(dto.getNiveauGravite());
        incident.setTypeIncident(dto.getTypeIncident());
        return incidentRepository.save(incident);
    }

    public List<IncidentSanitaire> getIncidentsByCycleBassinAssoc(Integer idCycleBassinAssoc) {
        return incidentRepository.findByCycleBassinAssocId(idCycleBassinAssoc);
    }

    public IncidentSanitaire getIncidentById(Integer id) {
        return incidentRepository.findById(id).orElse(null);
    }

    @Transactional
    public void resoudreIncident(Integer id) {
        IncidentSanitaire incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));
        incident.setEstResolu(true);
        incidentRepository.save(incident);
    }
}
