package com.example.orose.service;

import com.example.orose.dto.BassinDetailDTO;
import com.example.orose.dto.BassinGrilleDTO;
import com.example.orose.dto.CycleResumeDTO;
import com.example.orose.model.Bassin;
import com.example.orose.model.Cycle;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BassinGrilleService {

    private final BassinRepository bassinRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;

    public BassinGrilleService(BassinRepository bassinRepository,
                               CycleBassinAssocRepository cycleBassinAssocRepository) {
        this.bassinRepository = bassinRepository;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
    }

    public List<BassinGrilleDTO> getGrilleBassins() {
        return bassinRepository.findAll().stream()
                .sorted(Comparator.comparing(Bassin::getCode))
                .map(this::toGrilleDTO)
                .collect(Collectors.toList());
    }

    private BassinGrilleDTO toGrilleDTO(Bassin b) {
        BassinGrilleDTO dto = new BassinGrilleDTO();
        dto.setId(b.getId().longValue());
        dto.setCode(b.getCode());
        dto.setStatutCode(b.getStatutActuel().getCode());
        dto.setStatutLibelle(b.getStatutActuel().getLibelle());
        dto.setBadgeCss(badgeCss(b.getStatutActuel().getCode()));
        dto.setBloque("QUARANTAINE".equals(b.getStatutActuel().getCode()));

        Optional<CycleBassinAssoc> assocOpt =
                cycleBassinAssocRepository.findByBassinIdAndEstClotureFalse(b.getId().longValue());
        assocOpt.ifPresent(assoc -> {
            Cycle cycle = assoc.getCycle();
            dto.setCodeUniqueCycle(cycle.getCodeUniqueCycle());
            dto.setSemaineActuelle(assoc.getSemaineActuelle());
            dto.setPoidsMoyenActuel(assoc.getPoidsMoyenActuel());
            dto.setTauxAvancement(calculerTaux(cycle));
            dto.setJoursRestants(calculerJours(cycle));
            dto.setRecoltePossible(
                    assoc.getPoidsMoyenActuel() != null &&
                    assoc.getPoidsMoyenActuel().compareTo(BigDecimal.valueOf(15)) >= 0
            );
        });
        return dto;
    }

    public BassinDetailDTO getDetailBassin(Long idBassin) {
        Bassin b = bassinRepository.findById(idBassin)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));

        BassinDetailDTO dto = new BassinDetailDTO();
        dto.setId(b.getId().longValue());
        dto.setCode(b.getCode());
        dto.setSurfaceM2(b.getSurfaceM2());
        dto.setProfondeurMetre(b.getProfondeurMetre());
        dto.setMiseEnService(b.getCreatedAt() != null ? b.getCreatedAt().toLocalDate() : null);
        dto.setStatutCode(b.getStatutActuel().getCode());
        dto.setStatutLibelle(b.getStatutActuel().getLibelle());
        dto.setBadgeCss(badgeCss(b.getStatutActuel().getCode()));

        Optional<CycleBassinAssoc> assocOpt =
                cycleBassinAssocRepository.findByBassinIdAndEstClotureFalse(b.getId().longValue());
        assocOpt.ifPresent(assoc -> {
            Cycle cycle = assoc.getCycle();
            CycleResumeDTO cr = new CycleResumeDTO();
            cr.setIdCycleBassinAssoc(assoc.getId());
            cr.setCodeUniqueCycle(cycle.getCodeUniqueCycle());
            cr.setTauxAvancement(calculerTaux(cycle));
            cr.setSemaineActuelle(assoc.getSemaineActuelle());
            cr.setDureeTotaleSemaines(calculerDureeSemaines(cycle));
            cr.setDateDebut(cycle.getDateDebut());
            cr.setDateFinPrevue(cycle.getDateFinPrevue());
            cr.setEffectifInitial(assoc.getEffectifInitial());
            cr.setPoidsMoyenActuel(assoc.getPoidsMoyenActuel());
            dto.setCycleEnCours(cr);

            if ("QUARANTAINE".equals(b.getStatutActuel().getCode())) {
                dto.setNotification("Bassin en quarantaine. Accès restreint et contrôles sanitaires en cours.");
                dto.setNiveauNotification("DANGER");
            } else if (assoc.getPoidsMoyenActuel() != null &&
                       assoc.getPoidsMoyenActuel().compareTo(BigDecimal.valueOf(15)) >= 0) {
                dto.setNotification("Récolte possible (Poids moyen > 15g). Planifier la logistique.");
                dto.setNiveauNotification("INFO");
            }
        });
        return dto;
    }

    public String badgeCss(String code) {
        if (code == null) return "badge-vide";
        return switch (code) {
            case "ACTIF"         -> "badge-actif";
            case "EN_TRAITEMENT" -> "badge-traitement";
            case "QUARANTAINE"   -> "badge-quarantaine";
            case "RECOLTE"       -> "badge-recolte";
            case "PREPARATION"   -> "badge-preparation";
            default              -> "badge-vide";
        };
    }

    private Integer calculerTaux(Cycle cycle) {
        if (cycle.getDateDebut() == null || cycle.getDateFinPrevue() == null) return 0;
        long total = ChronoUnit.DAYS.between(cycle.getDateDebut(), cycle.getDateFinPrevue());
        long ecoule = ChronoUnit.DAYS.between(cycle.getDateDebut(), LocalDate.now());
        if (total <= 0) return 100;
        ecoule = Math.max(0, Math.min(ecoule, total));
        return (int) Math.round((double) ecoule / total * 100);
    }

    private Integer calculerJours(Cycle cycle) {
        if (cycle.getDateFinPrevue() == null) return null;
        long jours = ChronoUnit.DAYS.between(LocalDate.now(), cycle.getDateFinPrevue());
        return (int) Math.max(0, jours);
    }

    private Integer calculerDureeSemaines(Cycle cycle) {
        if (cycle.getDateDebut() == null || cycle.getDateFinPrevue() == null) return null;
        return (int) ChronoUnit.WEEKS.between(cycle.getDateDebut(), cycle.getDateFinPrevue());
    }
}
