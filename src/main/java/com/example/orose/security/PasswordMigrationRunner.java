package com.example.orose.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.orose.model.Utilisateur;
import com.example.orose.repository.UtilisateurRepository;

/**
 * One-shot au démarrage : détecte les utilisateurs avec un mot de passe
 * en clair (longueur != 60, format BCrypt) et le hashe en BCrypt.
 *
 * Sans cette migration, l'authentification rejette tout puisque
 * BCryptPasswordEncoder.matches() ne compare que des hashes valides.
 *
 * Idempotent : un mot de passe déjà BCrypt-hashé est laissé tel quel.
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UtilisateurRepository utilisateurRepository,
                                    PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int migres = 0;
        for (Utilisateur u : utilisateurRepository.findAll()) {
            String mdp = u.getMotDePasse();
            if (mdp == null || mdp.isBlank()) continue;
            if (estDejaBcrypt(mdp)) continue;

            u.setMotDePasse(passwordEncoder.encode(mdp));
            utilisateurRepository.save(u);
            migres++;
        }
        if (migres > 0) {
            log.info("PasswordMigrationRunner : {} mot(s) de passe migré(s) vers BCrypt", migres);
        }
    }

    private boolean estDejaBcrypt(String s) {
        return s.length() == 60 && (s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$"));
    }
}
