package com.example.orose.service.nourrissage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orose.dto.nourrissage.JournalDTO;
import com.example.orose.model.Aliment;
import com.example.orose.model.DistributionNourriture;
import com.example.orose.model.EntreeStockAliment;
import com.example.orose.model.MouvementStockAliment;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.AlimentRepository;
import com.example.orose.repository.EntreeStockAlimentRepository;
import com.example.orose.repository.UtilisateurRepository;
import com.example.orose.repository.nourrissage.DistributionNourritureRepository;
import com.example.orose.repository.stock.MouvementStockAlimentRepository;

@Service
public class NourrissageService {

    private final DistributionNourritureRepository repository;
    private final AlimentRepository alimentRepository;
    private final EntreeStockAlimentRepository entreeStockAlimentRepository;
    private final MouvementStockAlimentRepository mouvementStockAlimentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private static final Integer ID_UTILISATEUR_CONNECTE = 1;

    public NourrissageService(DistributionNourritureRepository distributionRepository,
            AlimentRepository alimentRepository,
            EntreeStockAlimentRepository entreeStockAlimentRepository,
            MouvementStockAlimentRepository mouvementStockAlimentRepository,
            UtilisateurRepository utilisateurRepository) {
        this.repository = distributionRepository;
        this.alimentRepository = alimentRepository;
        this.entreeStockAlimentRepository = entreeStockAlimentRepository;
        this.mouvementStockAlimentRepository = mouvementStockAlimentRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public void valider(Integer idDistribution,
            Integer idUtilisateur) {

        try {
            repository.validerNourrissage(idDistribution, idUtilisateur);
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String message = root.getMessage();
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
            throw new RuntimeException(message);
        }

        
    }

    private void decrementerStockAliment(Integer idDistribution, Integer idUtilisateur) {
        DistributionNourriture dist = repository.findById(Long.valueOf(idDistribution)).orElse(null);
        if (dist == null || dist.getQuantiteDonneeKg() == null) return;

        BigDecimal quantite = dist.getQuantiteDonneeKg();
        if (quantite.compareTo(BigDecimal.ZERO) <= 0) return;

        Utilisateur user = utilisateurRepository.findById(idUtilisateur.longValue()).orElse(null);
        if (user == null) return;

        List<EntreeStockAliment> lots = entreeStockAlimentRepository.findStocksDisponibles();
        BigDecimal aRetirer = quantite;

        for (EntreeStockAliment lot : lots) {
            if (aRetirer.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal retire = lot.getQuantiteRestanteKg().min(aRetirer);
            lot.setQuantiteRestanteKg(lot.getQuantiteRestanteKg().subtract(retire));
            entreeStockAlimentRepository.save(lot);

            MouvementStockAliment mvt = new MouvementStockAliment();
            mvt.setEntreeAliment(lot);
            mvt.setTypeMouvement("NOURRISSAGE");
            mvt.setQuantiteKg(retire);
            mvt.setMotif("Distribution #" + idDistribution + " validée");
            mvt.setDateMouvement(LocalDateTime.now());
            mvt.setUtilisateur(user);
            mouvementStockAlimentRepository.save(mvt);

            aRetirer = aRetirer.subtract(retire);
        }

        if (aRetirer.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Stock aliment insuffisant pour valider la distribution");
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