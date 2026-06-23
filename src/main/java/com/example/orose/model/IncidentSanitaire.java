package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_sanitaire")
@Data
public class IncidentSanitaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_cycle_bassin_assoc", nullable = false)
    private CycleBassinAssoc cycleBassinAssoc;

    @Column(name = "date_detection", nullable = false)
    private LocalDate dateDetection;

    @Column(name = "type_incident", nullable = false, length = 30)
    private String typeIncident;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "niveau_gravite", nullable = false, length = 20)
    private String niveauGravite;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    @Column(name = "est_resolu", nullable = false)
    private Boolean estResolu;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}