package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement_stock_medicament")
@Data
public class MouvementStockMedicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_entree_medicament", nullable = false)
    private EntreeStockMedicament entreeMedicament;

    @Column(name = "type_mouvement", nullable = false, length = 20)
    private String typeMouvement; // PERTE, DESTRUCTION, AJUSTEMENT

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantite;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;
}