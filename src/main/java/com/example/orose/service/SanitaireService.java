package com.example.orose.service;

import com.example.orose.dto.BassinEtatDTO;
import com.example.orose.dto.IncidentActifDTO;
import com.example.orose.dto.SanitaireDashboardDTO;
import com.example.orose.dto.SanitaireHistoriqueStatsDTO;
import com.example.orose.dto.IncidentDetailDTO;
import com.example.orose.model.Bassin;
import com.example.orose.model.IncidentSanitaire;
import com.example.orose.model.Traitement;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.IncidentSanitaireRepository;
import com.example.orose.repository.TraitementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SanitaireService {

    @Autowired
    private BassinRepository bassinRepository;
    @Autowired
    private IncidentSanitaireRepository incidentRepository;
    @Autowired
    private TraitementRepository traitementRepository;

    public SanitaireDashboardDTO getDashboardSanitaire() {
        List<Bassin> bassins = bassinRepository.findAll();

        long nbSains = 0;
        long nbEnTraitement = 0;
        long nbQuarantaine = 0;

        List<BassinEtatDTO> bassinsEtat = bassins.stream().map(b -> {
            BassinEtatDTO etat = new BassinEtatDTO();
            etat.setCode(b.getCode());
            String code = b.getStatutActuel().getCode();
            if ("EN_TRAITEMENT".equalsIgnoreCase(code)) {
                etat.setStatutDotClass("traitement");
            } else if ("QUARANTAINE".equalsIgnoreCase(code)) {
                etat.setStatutDotClass("quarantaine");
            } else {
                etat.setStatutDotClass("sain");
            }
            return etat;
        }).collect(Collectors.toList());

        for (Bassin b : bassins) {
            String code = b.getStatutActuel().getCode();
            if ("EN_TRAITEMENT".equalsIgnoreCase(code)) {
                nbEnTraitement++;
            } else if ("QUARANTAINE".equalsIgnoreCase(code)) {
                nbQuarantaine++;
            } else {
                nbSains++;
            }
        }

        List<IncidentSanitaire> incidentsActifs = incidentRepository.findByEstResoluFalse();
        List<IncidentActifDTO> actifsDTO = incidentsActifs.stream().map(inc -> {
            IncidentActifDTO dto = new IncidentActifDTO();
            dto.setId(inc.getId());
            dto.setBassinCode(inc.getCycleBassinAssoc().getBassin().getCode());
            dto.setTypeIncident(inc.getTypeIncident());
            dto.setGravite(inc.getNiveauGravite());
            dto.setDateDetection(inc.getDateDetection());
            List<Traitement> traitements = traitementRepository.findByIncidentId(inc.getId());
            if (traitements.isEmpty()) {
                dto.setStatutTraitement("AUCUN");
            } else if ("QUARANTAINE".equalsIgnoreCase(
                    inc.getCycleBassinAssoc().getBassin().getStatutActuel().getCode())) {
                dto.setStatutTraitement("QUARANTAINE");
            } else {
                dto.setStatutTraitement("EN_COURS");
            }
            return dto;
        }).collect(Collectors.toList());

        SanitaireDashboardDTO dash = new SanitaireDashboardDTO();
        dash.setNbBassinsSains(nbSains);
        dash.setNbBassinsEnTraitement(nbEnTraitement);
        dash.setNbBassinsQuarantaine(nbQuarantaine);
        dash.setNbIncidentsActifs(incidentsActifs.size());
        dash.setTotalBassins(bassins.size());
        dash.setIncidentsActifs(actifsDTO);
        dash.setBassinsEtat(bassinsEtat);

        return dash;
    }

    public Page<IncidentSanitaire> getHistoriqueSanitaire(Integer idBassin, String type, LocalDateTime debut,
            LocalDateTime fin, String statut, Pageable pageable) {
        List<IncidentSanitaire> all = incidentRepository.findAll();
        List<IncidentSanitaire> filtered = all.stream().filter(inc -> {
            if (idBassin != null
                    && !inc.getCycleBassinAssoc().getBassin().getId().equals(idBassin)) {
                return false;
            }
            if (type != null && !type.isEmpty() && !inc.getTypeIncident().equalsIgnoreCase(type)) {
                return false;
            }
            if (debut != null && inc.getCreatedAt().isBefore(debut)) {
                return false;
            }
            if (fin != null && inc.getCreatedAt().isAfter(fin)) {
                return false;
            }
            if (statut != null && !statut.isEmpty()) {
                boolean isResolu = "RESOLU".equalsIgnoreCase(statut);
                if (inc.getEstResolu() != isResolu) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        if (start > filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    public SanitaireHistoriqueStatsDTO getHistoriqueStats() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<IncidentSanitaire> all = incidentRepository.findAll();

        long total30j = all.stream()
                .filter(inc -> inc.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

        long resolved = all.stream().filter(IncidentSanitaire::getEstResolu).count();
        double tauxGuerison = all.isEmpty() ? 0.0 : (resolved * 100.0 / all.size());

        SanitaireHistoriqueStatsDTO stats = new SanitaireHistoriqueStatsDTO();
        stats.setTotalIncidents30j(total30j);
        stats.setTauxGuerison(tauxGuerison);

        all.stream()
                .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .ifPresent(last -> {
                    stats.setDernierBassinCode(last.getCycleBassinAssoc().getBassin().getCode());
                    stats.setDernierIncidentDate(
                            last.getDateDetection().format(DateTimeFormatter.ofPattern("dd/MM")));
                });

        return stats;
    }

    public IncidentDetailDTO getIncidentDetail(Integer idIncident) {
        IncidentSanitaire incident = incidentRepository.findById(idIncident).orElse(null);
        if (incident == null) {
            return null;
        }
        List<Traitement> traitements = traitementRepository.findByIncidentId(idIncident);
        IncidentDetailDTO dto = new IncidentDetailDTO();
        dto.setIncident(incident);
        dto.setTraitements(traitements);
        return dto;
    }
}
