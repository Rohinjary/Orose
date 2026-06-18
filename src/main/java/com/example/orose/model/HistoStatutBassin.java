package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "histo_statut_bassin")
@Data
public class HistoStatutBassin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_bassin", nullable = false)
    private Bassin bassin;

    @ManyToOne
    @JoinColumn(name = "id_statut_bassin", nullable = false)
    private StatutBassin statutBassin;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;
}