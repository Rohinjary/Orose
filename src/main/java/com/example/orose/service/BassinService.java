package com.example.orose.service;

import com.example.orose.dto.BassinDTO;
import com.example.orose.dto.HistoAvecAvantDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.orose.model.Bassin;
import com.example.orose.model.HistoStatutBassin;
import com.example.orose.model.StatutBassin;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.BassinRepository;
import com.example.orose.repository.CycleBassinAssocRepository;
import com.example.orose.repository.StatutBassinRepository;
import com.example.orose.repository.HistoStatutBassinRepository;
import com.example.orose.repository.UtilisateurRepository;

@Service
public class BassinService {
    private final BassinRepository bassinRepository;
    private final StatutBassinRepository statutBassinRepository;
    private final CycleBassinAssocRepository cycleBassinAssocRepository;
    private final StatutBassinService statutBassinService;
    private final HistoStatutBassinRepository histoStatutBassinRepository;
    private final UtilisateurRepository utilisateurRepository;

    public BassinService(BassinRepository bassinRepository,
                         StatutBassinRepository statutBassinRepository,
                         CycleBassinAssocRepository cycleBassinAssocRepository,
                         StatutBassinService statutBassinService,
                         HistoStatutBassinRepository histoStatutBassinRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.bassinRepository = bassinRepository;
        this.statutBassinRepository = statutBassinRepository;
        this.cycleBassinAssocRepository = cycleBassinAssocRepository;
        this.statutBassinService = statutBassinService;
        this.histoStatutBassinRepository = histoStatutBassinRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public Bassin creerBassin(BassinDTO dto, Long idUtilisateur) {
        if (bassinRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Le code du bassin doit être unique");
        }

        if (bassinRepository.count() >= 9) {
            throw new IllegalArgumentException("Il ne peut y avoir que 9 bassins maximum");
        }

        StatutBassin statutInitial = statutBassinRepository.findByCode("VIDE")
                .orElseThrow(() -> new IllegalArgumentException("Statut VIDE introuvable"));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Bassin bassin = new Bassin();
        bassin.setCode(dto.getCode());
        bassin.setSurfaceM2(dto.getSurface_m2());
        bassin.setNotes(dto.getNotes());
        bassin.setProfondeurMetre(dto.getProfondeur_metre());
        bassin.setStatutActuel(statutInitial);
        bassin.setCreatedAt(dto.getDateCreation().atStartOfDay());

        Bassin bassinSauvegarde = bassinRepository.save(bassin);

        HistoStatutBassin histoInitial = new HistoStatutBassin();
        histoInitial.setBassin(bassinSauvegarde);
        histoInitial.setStatutBassin(statutInitial);
        histoInitial.setUtilisateur(utilisateur);
        histoInitial.setMotif("Création du bassin");
        histoStatutBassinRepository.save(histoInitial);

        return bassinSauvegarde;
    }

    public void supprimerBassin(Long id) {
        Bassin bassin = bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));

        if (cycleBassinAssocRepository.existsByBassinIdAndEstClotureFalse(id)) {
            throw new IllegalStateException("Impossible de supprimer le bassin car il est associé à des cycles");
        }

        bassinRepository.delete(bassin);
    }

    public Bassin modifierBassin(Long id, BassinDTO dto) {
        Bassin bassin = bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));

        if (bassinRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Le code du bassin doit être unique");
        }

        bassin.setCode(dto.getCode());
        bassin.setSurfaceM2(dto.getSurface_m2());
        bassin.setProfondeurMetre(dto.getProfondeur_metre());
        bassin.setNotes(dto.getNotes());

        return bassinRepository.save(bassin);
    }

    public List<Bassin> listerBassins() {
        return bassinRepository.findAll();
    }

    public Bassin getBassinById(Long id) {
        return bassinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bassin introuvable"));
    }

    public void changerStatutBassin(Long idBassin, String nouveauStatut, String motif, Long idUtilisateur) {
        Bassin bassin = getBassinById(idBassin);
        String statutActuelCode = bassin.getStatutActuel().getCode();

        statutBassinService.validerTransition(statutActuelCode, nouveauStatut);

        StatutBassin nouveauStatutEntity = statutBassinRepository.findByCode(nouveauStatut)
                .orElseThrow(() -> new IllegalArgumentException("Nouveau statut introuvable"));

        bassin.setStatutActuel(nouveauStatutEntity);
        bassinRepository.save(bassin);

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        HistoStatutBassin histo = new HistoStatutBassin();
        histo.setBassin(bassin);
        histo.setStatutBassin(nouveauStatutEntity);
        histo.setUtilisateur(utilisateur);
        histo.setMotif(motif);
        histoStatutBassinRepository.save(histo);
    }

    public List<String> getTransitionsAutorisees(Long idBassin) {
        Bassin bassin = getBassinById(idBassin);
        String statutActuelCode = bassin.getStatutActuel().getCode();
        return statutBassinService.getTransitionsAutorisees(statutActuelCode);
    }

    public List<HistoStatutBassin> getHistoriqueStatuts(Long idBassin, LocalDateTime debut, LocalDateTime fin, String typeEtat) {
        return histoStatutBassinRepository.findByBassinIdOrderByDateChangementDesc(idBassin).stream()
            .filter(histo -> debut == null || !histo.getDateChangement().isBefore(debut))
            .filter(histo -> fin == null || !histo.getDateChangement().isAfter(fin))
            .filter(histo -> typeEtat == null || typeEtat.isBlank()
                || (histo.getStatutBassin() != null && typeEtat.equals(histo.getStatutBassin().getCode())))
            .collect(Collectors.toList());
    }

    public HistoStatutBassin getDernierStatut(Long idBassin) {
        return histoStatutBassinRepository.findTopByBassinIdOrderByDateChangementDesc(idBassin)
                .orElseThrow(() -> new IllegalArgumentException("Aucun historique trouvé pour ce bassin"));
    }

    public List<Bassin> getBassinsParStatut(String codeStatut) {
        return bassinRepository.findAll().stream()
            .filter(b -> codeStatut.equals(b.getStatutActuel().getCode()))
            .collect(Collectors.toList());
    }

    public List<HistoStatutBassin> getHistoriqueGlobal(LocalDateTime debut, LocalDateTime fin, String typeEtat) {
        return histoStatutBassinRepository.findAllByOrderByDateChangementDesc().stream()
            .filter(h -> debut == null || !h.getDateChangement().isBefore(debut))
            .filter(h -> fin == null || !h.getDateChangement().isAfter(fin))
            .filter(h -> typeEtat == null || typeEtat.isBlank()
                || (h.getStatutBassin() != null && typeEtat.equals(h.getStatutBassin().getCode())))
            .collect(Collectors.toList());
    }

    public List<HistoAvecAvantDTO> getHistoriqueAvecAvant(Long idBassin) {
        List<HistoStatutBassin> records =
                histoStatutBassinRepository.findByBassinIdOrderByDateChangementDesc(idBassin);
        return buildAvecAvant(records);
    }

    public List<HistoAvecAvantDTO> getHistoriqueGlobalAvecAvant(Long idBassin,
                                                                 LocalDateTime debut,
                                                                 LocalDateTime fin,
                                                                 String typeEtat) {
        List<HistoStatutBassin> records = idBassin != null
                ? histoStatutBassinRepository.findByBassinIdOrderByDateChangementDesc(idBassin)
                : histoStatutBassinRepository.findAllByOrderByDateChangementDesc();

        if (debut != null)
            records = records.stream().filter(h -> !h.getDateChangement().isBefore(debut)).collect(Collectors.toList());
        if (fin != null)
            records = records.stream().filter(h -> !h.getDateChangement().isAfter(fin)).collect(Collectors.toList());
        if (typeEtat != null && !typeEtat.isBlank())
            records = records.stream()
                    .filter(h -> h.getStatutBassin() != null && typeEtat.equals(h.getStatutBassin().getCode()))
                    .collect(Collectors.toList());

        return buildAvecAvant(records);
    }

    private List<HistoAvecAvantDTO> buildAvecAvant(List<HistoStatutBassin> records) {
        // Group all records by bassin and sort each group by date DESC to find "avant"
        Map<Integer, List<HistoStatutBassin>> parBassin = new LinkedHashMap<>();
        for (HistoStatutBassin h : records) {
            parBassin.computeIfAbsent(h.getBassin().getId(), k -> new ArrayList<>()).add(h);
        }

        Map<Integer, List<HistoStatutBassin>> sortedParBassin = new HashMap<>();
        parBassin.forEach((id, list) -> {
            List<HistoStatutBassin> sorted = list.stream()
                    .sorted(Comparator.comparing(HistoStatutBassin::getDateChangement).reversed())
                    .collect(Collectors.toList());
            sortedParBassin.put(id, sorted);
        });

        List<HistoAvecAvantDTO> result = new ArrayList<>();
        for (HistoStatutBassin h : records) {
            HistoAvecAvantDTO dto = new HistoAvecAvantDTO();
            dto.setId(h.getId());
            dto.setCodeBassin(h.getBassin().getCode());
            dto.setStatutApresCode(h.getStatutBassin().getCode());
            dto.setStatutApresLibelle(h.getStatutBassin().getLibelle());
            dto.setStatutApresBadge(badgeCss(h.getStatutBassin().getCode()));
            dto.setDateChangement(h.getDateChangement());
            String nom = h.getUtilisateur().getNom();
            String prenom = h.getUtilisateur().getPrenom();
            dto.setUtilisateurNom(nom + (prenom != null ? " " + prenom : ""));
            dto.setMotif(h.getMotif());

            List<HistoStatutBassin> bassinRecords = sortedParBassin.get(h.getBassin().getId());
            int idx = bassinRecords.indexOf(h);
            if (idx < bassinRecords.size() - 1) {
                StatutBassin avant = bassinRecords.get(idx + 1).getStatutBassin();
                dto.setStatutAvantCode(avant.getCode());
                dto.setStatutAvantLibelle(avant.getLibelle());
                dto.setStatutAvantBadge(badgeCss(avant.getCode()));
            } else {
                dto.setStatutAvantCode("—");
                dto.setStatutAvantLibelle("—");
                dto.setStatutAvantBadge("badge-vide");
            }
            result.add(dto);
        }

        result.sort(Comparator.comparing(HistoAvecAvantDTO::getDateChangement).reversed());
        return result;
    }

    private String badgeCss(String code) {
        if (code == null) return "badge-vide";
        return switch (code) {
            case "VIDE"         -> "badge-vide";
            case "ACTIF"         -> "badge-actif";
            case "EN_TRAITEMENT" -> "badge-traitement";
            case "QUARANTAINE"   -> "badge-quarantaine";
            case "RECOLTE"       -> "badge-recolte";
            case "PREPARATION"   -> "badge-preparation";
            default              -> "badge-vide";
        };
    }
}