package com.example.orose.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.orose.model.Role;
import com.example.orose.model.Utilisateur;
import com.example.orose.repository.RoleRepository;
import com.example.orose.repository.UtilisateurRepository;

/**
 * Seed de démarrage : crée les rôles ADMIN/DIR/TECH/RS s'ils n'existent pas,
 * puis crée 3 utilisateurs de test si la base d'utilisateurs est vide.
 *
 * Idempotent : si les rôles et utilisateurs existent déjà, ne fait rien.
 *
 * @Order(1) pour s'exécuter AVANT PasswordMigrationRunner (qui rehashe).
 */
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                            UtilisateurRepository utilisateurRepository,
                            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role admin = ensureRole("ADMIN", "Administrateur");
        Role dir = ensureRole("DIR", "Directeur");
        Role tech = ensureRole("TECH", "Technicien");
        Role rs = ensureRole("RS", "Responsable Sanitaire");

        ensureUser("admin@orose.mg", "Admin", "Système", "admin123", List.of(admin));
        ensureUser("directeur@orose.mg", "Directeur", "Demo", "dir123", List.of(dir));
        ensureUser("technicien@orose.mg", "Technicien", "Demo", "tech123", List.of(tech));
        ensureUser("sanitaire@orose.mg", "Responsable", "Sanitaire", "rs123", List.of(rs));
    }

    private Role ensureRole(String code, String libelle) {
        Optional<Role> existing = roleRepository.findByCode(code);
        if (existing.isPresent()) return existing.get();
        Role r = new Role();
        r.setCode(code);
        r.setLibelle(libelle);
        r.setUtilisateurs(new ArrayList<>());
        Role saved = roleRepository.save(r);
        log.info("DataInitializer : rôle '{}' créé", code);
        return saved;
    }

    private void ensureUser(String email, String nom, String prenom, String mdpClair, List<Role> roles) {
        if (utilisateurRepository.findByEmail(email).isPresent()) return;
        Utilisateur u = new Utilisateur();
        u.setEmail(email);
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setMotDePasse(passwordEncoder.encode(mdpClair));
        u.setStatut("ACTIF");
        u.setRoles(roles);
        utilisateurRepository.save(u);
        log.info("DataInitializer : utilisateur '{}' créé (mot de passe : {})", email, mdpClair);
    }
}
