package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement_stock_aliment")
@Data
public class MouvementStockAliment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_entree_aliment", nullable = false)
    private EntreeStockAliment entreeAliment;

    @Column(name = "type_mouvement", nullable = false, length = 20)
    private String typeMouvement; // PERTE, DESTRUCTION, AJUSTEMENT

    @Column(name = "quantite_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteKg;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;
}