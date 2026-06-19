package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventaire")
@Data
public class Inventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_produit", nullable = false, length = 20)
    private String typeProduit; // ALIMENT, MEDICAMENT, CREVETTE

    @ManyToOne
    @JoinColumn(name = "id_aliment")
    private Aliment aliment;

    @ManyToOne
    @JoinColumn(name = "id_medicament")
    private Medicament medicament;

    @ManyToOne
    @JoinColumn(name = "id_lot_crevette")
    private LotCrevette lotCrevette;

    @Column(name = "stock_theorique", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockTheorique;

    @Column(name = "stock_reel", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockReel;

    // colonne GENERATED ALWAYS en base -> jamais écrite par JPA
    @Column(insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal ecart;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    @Column(name = "date_inventaire", nullable = false)
    private LocalDateTime dateInventaire;
}