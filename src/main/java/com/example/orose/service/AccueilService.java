package com.example.orose.service;

import com.example.orose.model.Bassin;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.CycleBassinAssocRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.orose.dto.dashboard.EtatBassinActifDTO;

@Service
public class AccueilService {

    @Autowired
    private BassinRepository bassinRepository;

    @Autowired
    private CycleBassinAssocRepository cycleBassinAssocRepository;

    public EtatBassinActifDTO etatBassinActif(Bassin bassin) {
        EtatBassinActifDTO dto = new EtatBassinActifDTO();
        dto.setIdBassin(bassin.getId());
        dto.setCode(bassin.getCode());

        CycleBassinAssoc assoc = cycleBassinAssocRepository.findFirstByBassinIdAndEstClotureFalse(bassin.getId()).orElse(null);
        if (assoc != null) {
            dto.setSemaineActuelle(assoc.getSemaineActuelle() != null ? assoc.getSemaineActuelle() : 0);
            dto.setPoidsMoyen(assoc.getPoidsMoyenActuel() != null ? assoc.getPoidsMoyenActuel().floatValue() : 0f);
            
            if (assoc.getCycle() != null && assoc.getCycle().getDateFinPrevue() != null && assoc.getCycle().getDateDebut() != null) {
                LocalDate dateFinPrevue = assoc.getCycle().getDateFinPrevue();
                LocalDate dateDebut = assoc.getCycle().getDateDebut();
                
                long nbJoursRestants = ChronoUnit.DAYS.between(LocalDate.now(), dateFinPrevue);
                dto.setJoursRestants((int) Math.max(0, nbJoursRestants));
                
                long nbJoursPrevus = ChronoUnit.DAYS.between(dateDebut, dateFinPrevue);
                
                dto.setAvancement(calculerPourcentageAvancement((int) nbJoursPrevus, (int) nbJoursRestants));
            }
        }
        return dto;
    }

    public List<EtatBassinActifDTO> etatsTousBassinsActifs() {
        List<EtatBassinActifDTO> listeEtats = new ArrayList<>();
        List<Bassin> bassinsActifs = bassinRepository.findByStatutActuel_CodeIn(Collections.singletonList("ACTIF"));
        if (bassinsActifs != null) {
            for (Bassin bassin : bassinsActifs) {
                listeEtats.add(etatBassinActif(bassin));
            }
        }
        return listeEtats;
    } 

    private float calculerPourcentageAvancement(int nbJoursPrevus, int nbJoursRestants) {
        if (nbJoursPrevus <= 0) return 0f;
        float avancement = ((float) (nbJoursPrevus - nbJoursRestants) / nbJoursPrevus) * 100f;
        return Math.min(100f, Math.max(0f, avancement));
    }
}
