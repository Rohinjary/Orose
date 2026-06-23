package com.example.orose.service;

import com.example.orose.dto.CreneauStatusDTO;
import com.example.orose.dto.CreerDistributionDTO;
import com.example.orose.dto.NourrissageDashboardDTO;
import com.example.orose.dto.ValiderDistributionDTO;
import com.example.orose.model.*;
import com.example.orose.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NourrissageService {

    private final BassinRepository bassinRepository;
    private final CycleBassinRepository cycleBassinRepository;
    private final DistributionNourritureRepository distributionNourritureRepository;
    private final CreneauHoraireRepository creneauHoraireRepository;
    private final EntreeStockAlimentRepository entreeStockAlimentRepository;
    private final StatutBassinRepository statutBassinRepository;

    public NourrissageService(BassinRepository bassinRepository,
                               CycleBassinRepository cycleBassinRepository,
                               DistributionNourritureRepository distributionNourritureRepository,
                               CreneauHoraireRepository creneauHoraireRepository,
                               EntreeStockAlimentRepository entreeStockAlimentRepository,
                               StatutBassinRepository statutBassinRepository) {
        this.bassinRepository = bassinRepository;
        this.cycleBassinRepository = cycleBassinRepository;
        this.distributionNourritureRepository = distributionNourritureRepository;
        this.creneauHoraireRepository = creneauHoraireRepository;
        this.entreeStockAlimentRepository = entreeStockAlimentRepository;
        this.statutBassinRepository = statutBassinRepository;
    }

    public NourrissageDashboardDTO getDashboardNourrissage() {
        NourrissageDashboardDTO dashboard = new NourrissageDashboardDTO();
        LocalDate today = LocalDate.now();
        dashboard.setDate(today);

        // Get active bassins
        StatutBassin statutActif = statutBassinRepository.findByCode("ACTIF")
                .orElse(null);
        
        List<Bassin> bassinsActifs = statutActif != null 
                ? bassinRepository.findByStatutActuel(statutActif)
                : bassinRepository.findAll();

        dashboard.setTotalBassinsActifs(bassinsActifs.size());

        // Calculate stock information
        BigDecimal stockDisponible = entreeStockAlimentRepository.sumQuantiteRestante();
        if (stockDisponible == null) {
            stockDisponible = BigDecimal.ZERO;
        }
        dashboard.setStockDisponibleKg(stockDisponible);

        // Estimate autonomy (simplified: assume 50kg per day)
        Integer autonomieJours = stockDisponible.compareTo(BigDecimal.ZERO) > 0 
                ? stockDisponible.divide(new BigDecimal("50"), 0, java.math.RoundingMode.DOWN).intValue()
                : 0;
        dashboard.setAutonomieJours(autonomieJours);
        dashboard.setAlerteStock(autonomieJours < 7);

        // Get all creneaux
        List<CreneauHoraire> creneaux = creneauHoraireRepository.findAll();

        // Build bassin dashboard data
        List<NourrissageDashboardDTO.BassinDashboardDTO> bassinsDTO = new ArrayList<>();
        int repasDistribues = 0;
        int repasEnAttente = 0;
        int repasRetard = 0;

        LocalTime prochainRepasHeure = null;
        String prochainRepasBassin = null;

        for (Bassin bassin : bassinsActifs) {
            NourrissageDashboardDTO.BassinDashboardDTO bassinDTO = new NourrissageDashboardDTO.BassinDashboardDTO();
            bassinDTO.setId(bassin.getId());
            bassinDTO.setCode(bassin.getCode());

            // Get active cycle for this bassin
            Optional<CycleBassin> activeCycle = cycleBassinRepository.findByBassinIdAndEstClotureFalse(bassin.getId());
            if (activeCycle.isPresent()) {
                CycleBassin cycle = activeCycle.get();
                bassinDTO.setEspece(cycle.getEspece() != null ? cycle.getEspece().toString() : "Non défini");
            } else {
                bassinDTO.setEspece("Aucun cycle actif");
            }

            // Get distributions for each creneau
            CreneauStatusDTO matin = getCreneauStatus(bassin, "MATIN", today, creneaux);
            CreneauStatusDTO midi = getCreneauStatus(bassin, "MIDI", today, creneaux);
            CreneauStatusDTO soir = getCreneauStatus(bassin, "SOIR", today, creneaux);
            CreneauStatusDTO nuit = getCreneauStatus(bassin, "NUIT", today, creneaux);

            bassinDTO.setMatin(matin);
            bassinDTO.setMidi(midi);
            bassinDTO.setSoir(soir);
            bassinDTO.setNuit(nuit);

            // Count statistics
            repasDistribues += countStatut(matin, midi, soir, nuit, "NOURRI");
            repasEnAttente += countStatut(matin, midi, soir, nuit, "EN_ATTENTE");
            repasRetard += countStatut(matin, midi, soir, nuit, "RETARD");

            // Determine global status and find next meal
            String statutGlobal = determineStatutGlobal(matin, midi, soir, nuit);
            bassinDTO.setStatutGlobal(statutGlobal);

            // Track next meal
            if ("EN_ATTENTE".equals(statutGlobal) || "RETARD".equals(statutGlobal)) {
                LocalTime nextMealTime = findNextMealTime(matin, midi, soir, nuit);
                if (nextMealTime != null && (prochainRepasHeure == null || nextMealTime.isBefore(prochainRepasHeure))) {
                    prochainRepasHeure = nextMealTime;
                    prochainRepasBassin = bassin.getCode();
                }
            }

            bassinsDTO.add(bassinDTO);
        }

        dashboard.setBassins(bassinsDTO);
        dashboard.setRepasDistribues(repasDistribues);
        dashboard.setRepasEnAttente(repasEnAttente);
        dashboard.setRepasRetard(repasRetard);
        dashboard.setProchainRepasHeure(prochainRepasHeure);
        dashboard.setProchainRepasBassin(prochainRepasBassin);

        return dashboard;
    }

    private CreneauStatusDTO getCreneauStatus(Bassin bassin, String creneauLibelle, LocalDate date, List<CreneauHoraire> creneaux) {
        CreneauStatusDTO dto = new CreneauStatusDTO();
        dto.setCreneau(creneauLibelle);

        Optional<CreneauHoraire> creneauOpt = creneaux.stream()
                .filter(c -> creneauLibelle.equals(c.getLibelle()))
                .findFirst();

        if (creneauOpt.isEmpty()) {
            dto.setStatut("PRÉVU");
            return dto;
        }

        CreneauHoraire creneau = creneauOpt.get();

        // Get active cycle
        Optional<CycleBassin> cycleOpt = cycleBassinRepository.findByBassinIdAndEstClotureFalse(bassin.getId());
        if (cycleOpt.isEmpty()) {
            dto.setStatut("RUPTURE");
            return dto;
        }

        CycleBassin cycle = cycleOpt.get();

        // Find distribution for this creneau
        Optional<DistributionNourriture> distOpt = distributionNourritureRepository
                .findByCycleIdAndDateDistributionAndCreneauId(cycle.getId(), date, creneau.getId());

        if (distOpt.isEmpty()) {
            // No distribution planned - set default planned time based on creneau
            dto.setStatut("PRÉVU");
            dto.setHeurePrevue(getDefaultHeureForCreneau(creneauLibelle));
            return dto;
        }

        DistributionNourriture distribution = distOpt.get();
        dto.setStatut(distribution.getStatut());
        dto.setQuantitePrevueKg(distribution.getQuantitePrevueKg());
        dto.setQuantiteDonneeKg(distribution.getQuantiteDonneeKg());
        dto.setEstValide(distribution.getEstValide());
        dto.setHeurePrevue(getDefaultHeureForCreneau(creneauLibelle));

        // If status is RETARD, calculate delay
        if ("RETARD".equals(distribution.getStatut())) {
            LocalTime now = LocalTime.now();
            dto.setHeureReelle(now);
        }

        return dto;
    }

    private LocalTime getDefaultHeureForCreneau(String creneau) {
        return switch (creneau) {
            case "MATIN" -> LocalTime.of(8, 0);
            case "MIDI" -> LocalTime.of(12, 0);
            case "SOIR" -> LocalTime.of(18, 0);
            case "NUIT" -> LocalTime.of(23, 0);
            default -> null;
        };
    }

    private int countStatut(CreneauStatusDTO matin, CreneauStatusDTO midi, CreneauStatusDTO soir, CreneauStatusDTO nuit, String statut) {
        int count = 0;
        if (matin != null && statut.equals(matin.getStatut())) count++;
        if (midi != null && statut.equals(midi.getStatut())) count++;
        if (soir != null && statut.equals(soir.getStatut())) count++;
        if (nuit != null && statut.equals(nuit.getStatut())) count++;
        return count;
    }

    private String determineStatutGlobal(CreneauStatusDTO matin, CreneauStatusDTO midi, CreneauStatusDTO soir, CreneauStatusDTO nuit) {
        // Priority: RETARD > RUPTURE > EN_ATTENTE > NOURRI > PRÉVU
        if (hasStatut(matin, midi, soir, nuit, "RETARD")) return "RETARD";
        if (hasStatut(matin, midi, soir, nuit, "RUPTURE")) return "RUPTURE";
        if (hasStatut(matin, midi, soir, nuit, "EN_ATTENTE")) return "EN_ATTENTE";
        if (hasStatut(matin, midi, soir, nuit, "NOURRI")) return "NOURRI";
        return "PRÉVU";
    }

    private boolean hasStatut(CreneauStatusDTO matin, CreneauStatusDTO midi, CreneauStatusDTO soir, CreneauStatusDTO nuit, String statut) {
        if (matin != null && statut.equals(matin.getStatut())) return true;
        if (midi != null && statut.equals(midi.getStatut())) return true;
        if (soir != null && statut.equals(soir.getStatut())) return true;
        if (nuit != null && statut.equals(nuit.getStatut())) return true;
        return false;
    }

    private LocalTime findNextMealTime(CreneauStatusDTO matin, CreneauStatusDTO midi, CreneauStatusDTO soir, CreneauStatusDTO nuit) {
        LocalTime now = LocalTime.now();
        
        if (matin != null && "EN_ATTENTE".equals(matin.getStatut()) && matin.getHeurePrevue() != null && matin.getHeurePrevue().isAfter(now)) {
            return matin.getHeurePrevue();
        }
        if (midi != null && "EN_ATTENTE".equals(midi.getStatut()) && midi.getHeurePrevue() != null && midi.getHeurePrevue().isAfter(now)) {
            return midi.getHeurePrevue();
        }
        if (soir != null && "EN_ATTENTE".equals(soir.getStatut()) && soir.getHeurePrevue() != null && soir.getHeurePrevue().isAfter(now)) {
            return soir.getHeurePrevue();
        }
        if (nuit != null && "EN_ATTENTE".equals(nuit.getStatut()) && nuit.getHeurePrevue() != null && nuit.getHeurePrevue().isAfter(now)) {
            return nuit.getHeurePrevue();
        }
        
        // If all past times, return the earliest EN_ATTENTE or RETARD
        if (matin != null && ("EN_ATTENTE".equals(matin.getStatut()) || "RETARD".equals(matin.getStatut()))) {
            return matin.getHeurePrevue() != null ? matin.getHeurePrevue() : getDefaultHeureForCreneau("MATIN");
        }
        if (midi != null && ("EN_ATTENTE".equals(midi.getStatut()) || "RETARD".equals(midi.getStatut()))) {
            return midi.getHeurePrevue() != null ? midi.getHeurePrevue() : getDefaultHeureForCreneau("MIDI");
        }
        if (soir != null && ("EN_ATTENTE".equals(soir.getStatut()) || "RETARD".equals(soir.getStatut()))) {
            return soir.getHeurePrevue() != null ? soir.getHeurePrevue() : getDefaultHeureForCreneau("SOIR");
        }
        if (nuit != null && ("EN_ATTENTE".equals(nuit.getStatut()) || "RETARD".equals(nuit.getStatut()))) {
            return nuit.getHeurePrevue() != null ? nuit.getHeurePrevue() : getDefaultHeureForCreneau("NUIT");
        }
        
        return null;
    }

    public DistributionNourriture creerDistribution(CreerDistributionDTO dto) {
        // Get cycle
        Optional<CycleBassin> cycleOpt = cycleBassinRepository.findById(dto.getIdCycle());
        if (cycleOpt.isEmpty()) {
            throw new IllegalArgumentException("Cycle non trouvé");
        }

        // Get creneau
        Optional<CreneauHoraire> creneauOpt = creneauHoraireRepository.findByLibelle(dto.getCreneau());
        if (creneauOpt.isEmpty()) {
            throw new IllegalArgumentException("Créneau non trouvé");
        }

        // Create distribution
        DistributionNourriture distribution = new DistributionNourriture();
        distribution.setCycle(cycleOpt.get());
        distribution.setEntreeAliment(entreeStockAlimentRepository.findById(dto.getIdEntreeAliment()).orElse(null));
        distribution.setCreneau(creneauOpt.get());
        distribution.setDateDistribution(dto.getDateDistribution());
        distribution.setQuantitePrevueKg(dto.getQuantitePrevueKg());
        distribution.setQuantiteDonneeKg(BigDecimal.ZERO);
        distribution.setStatut("EN_ATTENTE");
        distribution.setEstValide(false);

        return distributionNourritureRepository.save(distribution);
    }

    public DistributionNourriture validerDistribution(ValiderDistributionDTO dto) {
        Optional<DistributionNourriture> distOpt = distributionNourritureRepository.findById(dto.getIdDistribution());
        if (distOpt.isEmpty()) {
            throw new IllegalArgumentException("Distribution non trouvée");
        }

        DistributionNourriture distribution = distOpt.get();
        distribution.setQuantiteDonneeKg(dto.getQuantiteDonneeKg());
        distribution.setStatut("NOURRI");
        distribution.setEstValide(true);

        // Update stock
        if (distribution.getEntreeAliment() != null) {
            EntreeStockAliment stock = distribution.getEntreeAliment();
            BigDecimal nouvelleQuantite = stock.getQuantiteRestanteKg().subtract(dto.getQuantiteDonneeKg());
            if (nouvelleQuantite.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Stock insuffisant");
            }
            stock.setQuantiteRestanteKg(nouvelleQuantite);
            entreeStockAlimentRepository.save(stock);
        }

        return distributionNourritureRepository.save(distribution);
    }
}
