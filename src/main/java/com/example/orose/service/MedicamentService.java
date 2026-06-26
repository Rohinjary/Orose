package com.example.orose.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.orose.model.Medicament;
import com.example.orose.repository.MedicamentRepository;

@Service
public class MedicamentService {

    @Autowired
    private MedicamentRepository medicamentRepository;

    // Enregistrer ou mettre à jour un médicament
    public Medicament enregistrerMedicament(Medicament medicament) {
        return medicamentRepository.save(medicament);
    }

    // Récupérer tous les médicaments
    public List<Medicament> listerTousLesMedicaments() {
        return medicamentRepository.findAll();
    }

    // Récupérer un médicament par son ID
    public Optional<Medicament> obtenirMedicamentParId(Integer id) {
        return medicamentRepository.findById(id);
    }

    // Supprimer un médicament
    public void supprimerMedicament(Integer id) {
        medicamentRepository.deleteById(id);
    }
}