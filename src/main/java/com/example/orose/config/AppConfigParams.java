package com.example.orose.config;

import java.math.BigDecimal;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.orose.model.Parametre;
import com.example.orose.repository.ParametreRepository;

@Component
public class AppConfigParams {
    @Autowired
    private ParametreRepository repository;

    public static int cycleParAn;
    public static int bassinParCycle;
    public static float prixKgCrevette;
    public static float poidsCibleGr;
    public static float tailleCibleMm;
    public static int plInitial;

    @PostConstruct
    public void init() {
        List<Parametre> params = repository.findAll();
        for (Parametre p : params) {
            BigDecimal val = p.getValeur();
            if (val == null) continue;
            
            switch (p.getLabel()) {
                case "cycle_par_an":
                    cycleParAn = (int) val.doubleValue();
                    break;
                case "bassin_par_cycle":
                    bassinParCycle = (int) val.doubleValue();
                    break;
                case "prix_kg_crevette":
                    prixKgCrevette = val.floatValue();
                    break;
                case "poids_cible_gr":
                    poidsCibleGr = val.floatValue();
                    break;
                case "taille_cible_mm":
                    tailleCibleMm = val.floatValue();
                    break;
                case "pl_initial":
                    plInitial = (int) val.doubleValue();
                    break;
            }
        }
    }
}
