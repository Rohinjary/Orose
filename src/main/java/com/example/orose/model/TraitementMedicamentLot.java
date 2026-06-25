package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "traitement_medicament_lot")
@Data
public class TraitementMedicamentLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_traitement", nullable = false)
    private Traitement traitement;

    @ManyToOne
    @JoinColumn(name = "id_entree_medicament", nullable = false)
    private EntreeStockMedicament entreeMedicament;

    @Column(name = "quantite_piquee", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantitePiquee;
}