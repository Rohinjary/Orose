package com.example.orose.service;

import com.example.orose.model.Bassin;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.LotCrevette;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.EntreeStockAlimentRepository;
import com.example.orose.repository.stock.LotCrevetteRepository;
import com.example.orose.service.stock.StockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.orose.dto.dashboard.EtatBassinActifDTO;
import com.example.orose.dto.stock.StockDashboardDTO;

@Service
public class AccueilService {

    @Autowired
    private BassinRepository bassinRepository;

    @Autowired
    private LotCrevetteRepository lotCrevetteRepository;
    
    @Autowired
    private EntreeStockAlimentRepository entreeAlimentRepository;

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

    public StockDashboardDTO getSituationStock(){
        StockDashboardDTO dto = new StockDashboardDTO();
        remplirStockCrevette(dto);
        remplirStockAliment(dto);
        return dto;
    }

    // fonctions de rovatiana
     public void remplirStockCrevette(StockDashboardDTO dto) {
        BigDecimal biomasse = lotCrevetteRepository.sumBiomasseDisponible();
        float biomasseVal = biomasse != null ? Math.max(0f, biomasse.floatValue()) : 0f;
        dto.setStockCrevetteKg(biomasseVal);
        // encore en dur mais il faut metre en parametre
        
        dto.setValeurCrevetteAr(biomasseVal * 40000f);
    }

    public void remplirStockAliment(StockDashboardDTO dto) {
        BigDecimal stockAliment = entreeAlimentRepository.sumQuantiteRestante();
        float stockAlimentVal = stockAliment != null ? Math.max(0f, stockAliment.floatValue()) : 0f;
        dto.setStockAlimentKg(stockAlimentVal);
        
        Float autonomie = estimerAutonomieAliment();
        dto.setAutonomieAlimentJours(autonomie != null ? Math.max(0f, autonomie) : 0f);
    }

    public Float estimerAutonomieAliment() {
        BigDecimal stockTotal = entreeAlimentRepository.sumQuantiteRestante();
        if (stockTotal == null || stockTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return 0f;
        }

        LocalDate dateSeuil = LocalDate.now().minusDays(7);
        BigDecimal conso = entreeAlimentRepository.sumEntreesDepuis(dateSeuil);
        return conso != null && conso.compareTo(BigDecimal.ZERO) > 0
                ? (stockTotal.floatValue() / conso.floatValue()) * 7f : 30f;
    }
}
