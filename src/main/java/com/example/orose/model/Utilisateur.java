package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "utilisateur")
@Data
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Column(nullable = false, length = 20)
    private String statut; // ACTIF, INACTIF

    @ManyToMany
    @JoinTable(
        name = "utilisateur_role",
        joinColumns = @JoinColumn(name = "id_utilisateur"),
        inverseJoinColumns = @JoinColumn(name = "id_role")
    )
    private List<Role> roles;
}