package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "statut_bassin")
@Data
public class StatutBassin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // PREPARATION, ACTIF, EN_TRAITEMENT, RECOLTE, QUARANTAINE

    @Column(nullable = false, length = 50)
    private String libelle;
}