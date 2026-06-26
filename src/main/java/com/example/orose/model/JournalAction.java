package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_action")
@Data
public class JournalAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 50)
    private String module;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entite_cible", length = 100)
    private String entiteCible;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;
}