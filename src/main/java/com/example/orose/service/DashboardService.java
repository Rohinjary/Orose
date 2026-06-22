package com.example.orose.service;

import com.example.orose.dto.BassinDashboardDTO;
import com.example.orose.dto.CreneauPlanningDTO;
import com.example.orose.model.Bassin;
import com.example.orose.model.CreneauHoraire;
import com.example.orose.model.DistributionNourriture;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.CreneauHoraireRepository;
import com.example.orose.repository.DistributionNourritureRepository;
import com.example.orose.utils.RegleCreneau;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final BassinRepository bassinRepository;
    private final DistributionNourritureRepository distRepository;
    private final CreneauHoraireRepository creneauRepository;

    public DashboardService(BassinRepository bassinRepository,
            DistributionNourritureRepository distRepository,
            CreneauHoraireRepository creneauRepository) {
        this.bassinRepository = bassinRepository;
        this.distRepository = distRepository;
        this.creneauRepository = creneauRepository;
    }

    // public List<BassinDashboardDTO> getPlanningDuJour() {
    // LocalDate aujourdhui = LocalDate.now();
    // List<Bassin> bassins = bassinRepository.findAll();
    // List<DistributionNourriture> realisations =
    // distRepository.findByDateDistribution(aujourdhui);

    // return bassins.stream().map(bassin -> {
    // BassinDashboardDTO dto = new BassinDashboardDTO();
    // dto.setId(bassin.getId().longValue());
    // dto.setCode(bassin.getCode());
    // dto.setNomCycle(bassin.getStatutActuel() != null ?
    // bassin.getStatutActuel().getLibelle() : "N/A");
    // dto.setCreneaux(calculerStatuts(bassin, realisations));
    // return dto;
    // }).collect(Collectors.toList());
    // }
    public List<BassinDashboardDTO> getPlanningDuJour() {
        LocalDate aujourdhui = LocalDate.now();
        List<Bassin> bassins = bassinRepository.findAll();
        System.out.println("DEBUG SERVICE: Nombre de bassins trouvés : " + bassins.size());

        // Récupération des distributions du jour pour éviter plusieurs requêtes en
        // boucle
        List<DistributionNourriture> realisations = distRepository.findByDateDistribution(aujourdhui);

        List<BassinDashboardDTO> result = bassins.stream().map(bassin -> {
            BassinDashboardDTO dto = new BassinDashboardDTO();
            dto.setCode(bassin.getCode());

            // Sécurisation du statut (important car un bassin peut avoir un statut null)
            if (bassin.getStatutActuel() != null) {
                dto.setNomCycle(bassin.getStatutActuel().getLibelle());
            } else {
                dto.setNomCycle("Aucun cycle");
            }

            // Calcul des créneaux
            dto.setCreneaux(calculerStatuts(bassin, realisations));

            return dto;
        }).collect(Collectors.toList());

        System.out.println("DEBUG SERVICE: DTOs générés : " + result.size());
        return result;
    }

    private List<CreneauPlanningDTO> calculerStatuts(Bassin b, List<DistributionNourriture> realisations) {
        List<CreneauHoraire> tousLesCreneaux = creneauRepository.findAllByOrderByOrdreAsc();

        return tousLesCreneaux.stream().map(c -> {
            CreneauPlanningDTO cp = new CreneauPlanningDTO();
            cp.setIdBassin(b.getId().longValue());
            cp.setIdCreneau(c.getId().longValue());
            cp.setNomCreneau(c.getLibelle());

            RegleCreneau regle;
            try {
                regle = RegleCreneau.valueOf(c.getLibelle());
            } catch (Exception e) {
                cp.setStatut("CONFIG_ERREUR");
                cp.setCssClass("badge-neutral"); // Couleur par défaut erreur
                return cp;
            }

            // Recherche de la distribution existante
            Optional<DistributionNourriture> dist = realisations.stream()
                    .filter(d -> d.getCycleBassinAssoc().getBassin().getId().equals(b.getId()))
                    .filter(d -> d.getCreneau().getId().equals(c.getId()))
                    .findFirst();

            String qtePrevue = regle.getQuantitePrevue() + "kg";

            if (dist.isPresent()) {
                cp.setStatut("NOURRI");
                cp.setCssClass("badge-success"); // Vert
                cp.setInfoComplementaire(dist.get().getDateDistribution().toString());
                cp.setQuantiteInfo(
                        dist.get().getQuantiteDonneeKg() + "kg / " + dist.get().getQuantitePrevueKg() + "kg");
            } else if (LocalTime.now().isAfter(regle.getFin())) {
                cp.setStatut("RETARD");
                cp.setCssClass("badge-danger"); // Rouge
                long mins = Duration.between(regle.getFin(), LocalTime.now()).toMinutes();
                cp.setMinutesRetard(mins); // Remplissage du nouveau champ
                cp.setInfoComplementaire("Depuis " + Math.abs(mins) + " min");
                cp.setQuantiteInfo("0kg / " + qtePrevue);
            } else {
                cp.setStatut("EN_ATTENTE");
                cp.setCssClass("badge-warning"); // Jaune/Orange
                cp.setInfoComplementaire("Prévu " + regle.getDebut());
                cp.setQuantiteInfo("0kg / " + qtePrevue);
            }
            return cp;
        }).collect(Collectors.toList());
    }
}