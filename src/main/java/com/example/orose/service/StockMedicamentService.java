package com.example.orose.service;

import com.example.orose.dto.EntreeStockMedicamentDTO;
import com.example.orose.model.EntreeStockMedicament;
import com.example.orose.model.Medicament;
import com.example.orose.model.MouvementStockMedicament;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.EntreeStockMedicamentRepository;
import com.example.orose.repository.MouvementStockMedicamentRepository;
import com.example.orose.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockMedicamentService {

    @Autowired
    private EntreeStockMedicamentRepository entreeRepository;
    @Autowired
    private MouvementStockMedicamentRepository mouvementRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Transactional
    public EntreeStockMedicament enregistrerEntreeMedicament(EntreeStockMedicamentDTO dto) {
        Utilisateur responsable = utilisateurRepository.findById(Long.valueOf(dto.getIdResponsable()))
                .orElseThrow(() -> new RuntimeException("Responsable non trouvé"));

        Medicament medicament = new Medicament();
        medicament.setId(dto.getIdMedicament()); 

        EntreeStockMedicament entree = new EntreeStockMedicament();
        entree.setMedicament(medicament);
        entree.setQuantite(dto.getQuantite());
        entree.setQuantiteRestante(dto.getQuantite());
        entree.setPrixTotalAr(dto.getPrixTotalAr());
        entree.setDateExpiration(dto.getDateExpiration());
        entree.setDateReception(java.time.LocalDate.now());
        entree.setResponsable(responsable);

        return entreeRepository.save(entree);
    }

    public List<EntreeStockMedicament> getEntreesStockMedicament() {
        return entreeRepository.findAll();
    }

    @Transactional
    public MouvementStockMedicament enregistrerPerteMedicament(Integer idEntreeStock, BigDecimal quantite, String motif, Integer idUtilisateur) {
        EntreeStockMedicament entree = entreeRepository.findById(idEntreeStock)
                .orElseThrow(() -> new RuntimeException("Entrée de stock non trouvée"));
        Utilisateur responsable = utilisateurRepository.findById(Long.valueOf(idUtilisateur))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (entree.getQuantiteRestante().compareTo(quantite) < 0) {
            throw new RuntimeException("Quantité insuffisante");
        }

        entree.setQuantiteRestante(entree.getQuantiteRestante().subtract(quantite));
        entreeRepository.save(entree);

        MouvementStockMedicament mouv = new MouvementStockMedicament();
        mouv.setEntreeMedicament(entree);
        mouv.setTypeMouvement("PERTE");
        mouv.setQuantite(quantite);
        mouv.setMotif(motif);
        mouv.setDateMouvement(LocalDateTime.now());
        mouv.setResponsable(responsable);

        return mouvementRepository.save(mouv);
    }

    public List<MouvementStockMedicament> getMouvementsMedicamentByEntree(Integer idEntreeStock) {
        return mouvementRepository.findByEntreeMedicamentId(idEntreeStock);
    }
}