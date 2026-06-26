package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerte")
@Data
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_alerte", nullable = false, length = 50)
    private String typeAlerte;

    @Column(nullable = false, length = 10)
    private String niveau;

    @Column(name = "module_source", nullable = false, length = 30)
    private String moduleSource;

    @ManyToOne
    @JoinColumn(name = "id_cycle_bassin_assoc")
    private CycleBassinAssoc cycleBassinAssoc;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "est_resolue", nullable = false)
    private Boolean estResolue;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    @ManyToOne
    @JoinColumn(name = "id_resolu_par")
    private Utilisateur resoluPar;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}