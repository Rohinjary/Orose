package com.example.orose.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "role")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // ADMIN, TECH, RS, DIR

    @Column(nullable = false, length = 50)
    private String libelle;

    @ManyToMany(mappedBy = "roles")
    private List<Utilisateur> utilisateurs;
}