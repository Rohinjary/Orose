package com.example.orose.service;

import com.example.orose.model.Alerte;
import com.example.orose.model.CycleBassinAssoc;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.AlerteRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final UtilisateurRepository utilisateurRepository;

    public AlerteService(AlerteRepository alerteRepository,
                         CycleBassinAssocRepository cycleBassinAssocRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.alerteRepository = alerteRepository;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Alerte> getAlertesBiologiques() {
        return alerteRepository.findByModuleSourceAndEstResolueFalseOrderByDateCreationDesc("BIOLOGIQUE");
    }

    public void resoudreAlerte(Long idAlerte, Long idUtilisateur) {
        Alerte alerte = alerteRepository.findById(idAlerte)
                .orElseThrow(() -> new EntityNotFoundException("Alerte introuvable : " + idAlerte));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + idUtilisateur));

        alerte.setEstResolue(true);
        alerte.setDateResolution(LocalDateTime.now());
        alerte.setResoluPar(utilisateur);
        alerteRepository.save(alerte);
    }

    public void creerAlerteSiAbsente(Integer idCycleBassinAssoc, String typeAlerte,
                                     String niveau, String message) {
        boolean existe = alerteRepository.existsByCycleBassinAssocIdAndTypeAlerteAndEstResolueFalse(
                idCycleBassinAssoc, typeAlerte);
        if (!existe) {
            CycleBassinAssoc assoc = cycleBassinAssocRepository.findById(idCycleBassinAssoc.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("CycleBassinAssoc introuvable : " + idCycleBassinAssoc));

            Alerte alerte = new Alerte();
            alerte.setCycleBassinAssoc(assoc);
            alerte.setTypeAlerte(typeAlerte);
            alerte.setNiveau(niveau);
            alerte.setModuleSource("BIOLOGIQUE");
            alerte.setMessage(message);
            alerte.setEstResolue(false);
            alerteRepository.save(alerte);
        }
    }

    public void creerAlerteDeRecolte(CycleBassinAssoc assoc) {
        creerAlerteSiAbsente(assoc.getId(), "RECOLTE_POSSIBLE", "VERT",
                "Calibre de récolte atteint pour le bassin " + assoc.getBassin().getCode()
                        + " (cycle " + assoc.getCycle().getCodeUniqueCycle() + ")");
    }
}
