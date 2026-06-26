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

    // 1. Récupération du journal des activités pour la journée en cours
    public List<DistributionNourriture> getJournalDuJour() {
        return repository.findJournalDuJour(LocalDate.now());
    }

    // 2. Récupération de l'historique filtré (Période, Bassin)
    // Note: Vous pouvez passer 'null' pour les paramètres optionnels
    public List<DistributionNourriture> getHistorique(LocalDate debut, LocalDate fin, String codeBassin) {
        return repository.findHistoriqueFiltre(debut, fin, codeBassin);
    }

    // 3. Calcul de la statistique de consommation totale (KPI pour le CDC)
    public BigDecimal getConsommationTotale(String codeBassin, LocalDate debut, LocalDate fin) {
        BigDecimal total = repository.sumQuantiteByBassinAndPeriode(codeBassin, debut, fin);
        return (total != null) ? total : BigDecimal.ZERO;
    }

    // Dans NourrissageService.java, supprimez les @Query et remplacez par :
    public List<DistributionNourriture> getHistoriqueFiltre(LocalDate date, String bassinCode, Long cycleId,
            Long creneauId) {
        return repository.findByFilters(date, bassinCode, cycleId, creneauId);
    }

    public List<JournalDTO> getJournalActivites() {
        return repository.findAllJournalComplet().stream()
                .map(d -> new JournalDTO(
                        d.getId(), // Si id est Integer dans le DTO, ça passe
                        d.getDateDistribution(),
                        d.getHeureNourrissage(),
                        d.getCycleBassinAssoc().getBassin().getCode(),
                        (d.getAliment() != null) ? d.getAliment().getLibelle() : "N/A", // CORRIGÉ : getLibelle()
                        d.getQuantiteDonneeKg(),
                        (d.getResponsable() != null) ? d.getResponsable().getNom() : "Admin", // CORRIGÉ : getNom()
                        d.getStatut()))
                .collect(Collectors.toList());
    }

    public List<DistributionNourriture> getJournalComplet() {
        return repository.findAllJournalComplet();
    }

    public List<JournalDTO> getHistoriqueFiltreDTO(LocalDate date, String bassinCode, Long cycleId, Long creneauId) {
        // Utilisation de findByFilters qui accepte bien les 4 paramètres
        return repository.findByFilters(date, bassinCode, cycleId, creneauId).stream()
                .map(d -> new JournalDTO(
                        d.getId(),
                        d.getDateDistribution(),
                        d.getHeureNourrissage(),
                        d.getCycleBassinAssoc().getBassin().getCode(),
                        (d.getAliment() != null) ? d.getAliment().getLibelle() : "N/A",
                        d.getQuantiteDonneeKg(),
                        (d.getResponsable() != null) ? d.getResponsable().getNom() : "Admin",
                        d.getStatut()))
                .collect(Collectors.toList());
    }

}