package com.example.orose.service;

import com.example.orose.dto.TraitementDTO;
import com.example.orose.model.EntreeStockMedicament;
import com.example.orose.model.IncidentSanitaire;
import com.example.orose.model.Traitement;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.EntreeStockMedicamentRepository;
import com.example.orose.repository.IncidentSanitaireRepository;
import com.example.orose.repository.TraitementRepository;
import com.example.orose.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TraitementService {

    @Autowired
    private TraitementRepository traitementRepository;
    @Autowired
    private IncidentSanitaireRepository incidentRepository;
    @Autowired
    private EntreeStockMedicamentRepository entreeStockRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Transactional
    public Traitement enregistrerTraitement(TraitementDTO dto) {
        IncidentSanitaire incident = incidentRepository.findById(dto.getIdIncident())
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));
        EntreeStockMedicament entree = entreeStockRepository.findById(dto.getIdEntreeMedicament())
                .orElseThrow(() -> new RuntimeException("Entrée de médicament non trouvée"));
        Utilisateur responsable = utilisateurRepository.findById(dto.getIdResponsable())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (entree.getQuantiteRestante().compareTo(dto.getQuantiteUtilisee()) < 0) {
            throw new RuntimeException("Stock insuffisant pour ce médicament");
        }

        Traitement traitement = new Traitement();
        traitement.setIncident(incident);
        traitement.setEntreeMedicament(entree);
        traitement.setDosage(dto.getDosage());
        traitement.setDureeJours(dto.getDureeJours());
        traitement.setDateDebut(dto.getDateDebut());
        traitement.setQuantiteUtilisee(dto.getQuantiteUtilisee());
        traitement.setResponsable(responsable);
        traitement.setCreatedAt(LocalDateTime.now());

        return traitementRepository.save(traitement);
    }

    public List<Traitement> getTraitementsByIncident(Integer idIncident) {
        return traitementRepository.findByIncidentId(idIncident);
    }

    public Traitement getTraitementById(Integer id) {
        return traitementRepository.findById(id).orElse(null);
    }
}