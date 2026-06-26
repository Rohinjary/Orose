package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cycle")
@Data
public class Cycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code_unique_cycle", nullable = false, unique = true, length = 50)
    private String codeUniqueCycle;

    @ManyToOne
    @JoinColumn(name = "id_espece", nullable = false)
    private EspeceCrevette espece;

    @ManyToOne
    @JoinColumn(name = "id_technicien")
    private Utilisateur technicien;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin_prevue", nullable = false)
    private LocalDate dateFinPrevue;

    @Column(name = "est_cloture", nullable = false)
    private Boolean estCloture = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
