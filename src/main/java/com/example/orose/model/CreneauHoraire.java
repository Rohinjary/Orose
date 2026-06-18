package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "creneau_horaire")
@Data
public class CreneauHoraire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String libelle; // MATIN, MIDI, SOIR, NUIT

    @Column(nullable = false)
    private Integer ordre;
}