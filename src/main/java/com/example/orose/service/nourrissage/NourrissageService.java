package com.example.orose.service.nourrissage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orose.dto.nourrissage.FcrDTO;
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

    @PersistenceContext
    private EntityManager entityManager;

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

    private void enregistrerMouvementsAliment(Integer idDistribution, Integer idUtilisateur) {
        entityManager.clear();
        List<Object[]> lots = entityManager.createNativeQuery(
                "SELECT dnl.id_entree_aliment, dnl.quantite_piquee_kg " +
                "FROM distribution_nourriture_lot dnl WHERE dnl.id_distribution = :id")
                .setParameter("id", idDistribution)
                .getResultList();

        if (lots.isEmpty()) return;

        Utilisateur user = utilisateurRepository.findById(idUtilisateur.longValue()).orElse(null);
        if (user == null) return;

        for (Object[] lot : lots) {
            Integer entreeAlimentId = ((Number) lot[0]).intValue();
            BigDecimal quantite = (BigDecimal) lot[1];

            EntreeStockAliment entree = entityManager.getReference(EntreeStockAliment.class, entreeAlimentId);

            MouvementStockAliment mvt = new MouvementStockAliment();
            mvt.setEntreeAliment(entree);
            mvt.setTypeMouvement("NOURRISSAGE");
            mvt.setQuantiteKg(quantite);
            mvt.setMotif("Distribution #" + idDistribution + " validée");
            mvt.setDateMouvement(LocalDateTime.now());
            mvt.setUtilisateur(user);
            mouvementStockAlimentRepository.save(mvt);
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
        List<DistributionNourriture> distributions = (date == null)
                ? repository.findByFiltersSansDate(bassinCode, cycleId, creneauId)
                : repository.findByFilters(date, bassinCode, cycleId, creneauId);

        return distributions.stream()
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

    public FcrDTO calculerFcrParCycle(Long cycleId) {
        if (cycleId == null) {
            return null;
        }

        List<FcrDTO> lignes = calculerFcrParBassins(cycleId, null);
        if (lignes.isEmpty()) {
            return null;
        }

        BigDecimal totalAlimentsKg = lignes.stream()
                .map(FcrDTO::getTotalAlimentsKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal biomasseProduiteKg = lignes.stream()
                .map(FcrDTO::getBiomasseProduiteKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BigDecimal> fcrCalculables = lignes.stream()
                .filter(ligne -> ligne.getBiomasseProduiteKg().compareTo(BigDecimal.ZERO) > 0)
                .map(FcrDTO::getFcrReel)
                .collect(Collectors.toList());

        if (fcrCalculables.isEmpty()) {
            return new FcrDTO(lignes.get(0).getCodeCycle(), "Moyenne cycle", totalAlimentsKg, biomasseProduiteKg);
        }

        BigDecimal sommeFcr = fcrCalculables.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal moyenneFcr = sommeFcr.divide(BigDecimal.valueOf(fcrCalculables.size()), 2, RoundingMode.HALF_UP);
        return new FcrDTO(lignes.get(0).getCodeCycle(), "Moyenne cycle", totalAlimentsKg, biomasseProduiteKg,
                moyenneFcr);
    }

    public List<FcrDTO> calculerFcrParBassins(Long cycleId, String bassinCode) {
        if (cycleId == null) {
            return List.of();
        }

        String codeBassinFiltre = (bassinCode == null || bassinCode.isBlank()) ? null : bassinCode;
        List<Object[]> rows = repository.findFcrParBassins(cycleId, codeBassinFiltre);
        List<FcrDTO> resultats = new ArrayList<>();

        for (Object[] row : rows) {
            resultats.add(new FcrDTO(
                    (String) row[0],
                    (String) row[1],
                    toBigDecimal(row[2]),
                    toBigDecimal(row[3])));
        }

        return resultats;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
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
