package com.example.orose.service.nourrissage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.orose.dto.nourrissage.JournalDTO;
import com.example.orose.model.Aliment;
import com.example.orose.model.DistributionNourriture;
import com.example.orose.repository.AlimentRepository;
import com.example.orose.repository.nourrissage.DistributionNourritureRepository;

@Service
public class NourrissageService {

    private final DistributionNourritureRepository repository;
    private final AlimentRepository alimentRepository;
    private static final Integer ID_UTILISATEUR_CONNECTE = 1; // Remplacer par l'ID de la session utilisateur si
                                                              // nécessaire

    public NourrissageService(DistributionNourritureRepository distributionRepository,
            AlimentRepository alimentRepository) {
        this.repository = distributionRepository;
        this.alimentRepository = alimentRepository;
    }

    public void valider(Integer idDistribution,
            Integer idUtilisateur) {

        try {

            repository.validerNourrissage(
                    idDistribution,
                    idUtilisateur);

        }

        catch (Exception e) {

            Throwable root = e;

            while (root.getCause() != null) {
                root = root.getCause();
            }

            String message = root.getMessage();

            System.out.println("MESSAGE BRUT = " + message);

            if (message != null) {

                message = message.replace("&nbsp;", " ");

                int idx = message.indexOf("Où");
                if (idx > -1) {
                    message = message.substring(0, idx).trim();
                }

                idx = message.indexOf("Where:");
                if (idx > -1) {
                    message = message.substring(0, idx).trim();
                }

                message = message.replace("ERROR:", "").trim();
            }

            System.out.println("MESSAGE NETTOYE = " + message);

            throw new RuntimeException(message);
        }
    }

    public void enregistrer(String codeBassin,
            Integer idAliment,
            BigDecimal quantiteKg,
            Integer idUtilisateur,
            LocalDate dateDistribution,
            LocalTime heure) {

        try {

            repository.enregistrerDistributionManuelle(
                    codeBassin,
                    idAliment,
                    quantiteKg,
                    idUtilisateur,
                    dateDistribution,
                    heure);
        }

        catch (Exception e) {

            Throwable root = e;

            while (root.getCause() != null) {
                root = root.getCause();
            }

            String message = root.getMessage();

            System.out.println("MESSAGE BRUT = " + message);

            if (message != null) {

                message = message.replace("&nbsp;", " ");

                int idx = message.indexOf("Où");
                if (idx > -1) {
                    message = message.substring(0, idx).trim();
                }

                idx = message.indexOf("Where:");
                if (idx > -1) {
                    message = message.substring(0, idx).trim();
                }

                message = message.replace("ERROR:", "").trim();
            }

            System.out.println("MESSAGE NETTOYE = " + message);

            throw new RuntimeException(message);
        }

    }

    public List<Aliment> getAlimentsDisponibles() {
        return alimentRepository.findAll();
    }

    // 1. Journal complet (pour votre page historique)
    public List<JournalDTO> getJournalActivites() {
        return repository.findAllJournalComplet().stream()
                .map(this::mapToJournalDTO)
                .collect(Collectors.toList());
    }

    // 2. Historique filtré (version DTO)
    public List<JournalDTO> getHistoriqueFiltreDTO(LocalDate date, String bassinCode, Long cycleId, Long creneauId) {
        return repository.findByFilters(date, bassinCode, cycleId, creneauId).stream()
                .map(this::mapToJournalDTO)
                .collect(Collectors.toList());
    }

    // 3. Méthodes utilitaires (Entités pures)
    public List<DistributionNourriture> getJournalDuJour() {
        return repository.findJournalDuJour(LocalDate.now());
    }

    public List<DistributionNourriture> getHistorique(LocalDate debut, LocalDate fin, String codeBassin) {
        return repository.findHistoriqueFiltre(debut, fin, codeBassin);
    }

    public List<DistributionNourriture> getHistoriqueFiltre(LocalDate date, String bassinCode, Long cycleId,
            Long creneauId) {
        return repository.findByFilters(date, bassinCode, cycleId, creneauId);
    }

    public List<DistributionNourriture> getJournalComplet() {
        return repository.findAllJournalComplet();
    }

    // 4. Moteur de transformation (Le cœur du nettoyage)
    private JournalDTO mapToJournalDTO(DistributionNourriture d) {
        return new JournalDTO(
                d.getId(),
                d.getDateDistribution(),
                d.getHeureNourrissage(),
                (d.getCycleBassinAssoc() != null && d.getCycleBassinAssoc().getBassin() != null)
                        ? d.getCycleBassinAssoc().getBassin().getCode()
                        : "N/A",
                (d.getAliment() != null) ? d.getAliment().getLibelle() : "Non défini",
                (d.getQuantiteDonneeKg() != null) ? d.getQuantiteDonneeKg() : BigDecimal.ZERO,
                (d.getResponsable() != null) ? d.getResponsable().getNom() : "Admin",
                (d.getStatut() != null) ? d.getStatut() : "EN_ATTENTE");
    }

}