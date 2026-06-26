package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "espece_crevette")
@Data
public class EspeceCrevette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom_scientifique", nullable = false, length = 100)
    private String nomScientifique;

    @Column(name = "nom_courant", nullable = false, length = 50)
    private String nomCourant;
}