package com.example.orose.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.orose.model.Utilisateur;
import com.example.orose.repository.UtilisateurRepository;

/**
 * Charge l'utilisateur depuis la base par email pour Spring Security.
 * Mappe chaque Role.code → autorité "ROLE_<code>".
 */
@Service
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));

        boolean actif = "ACTIF".equalsIgnoreCase(u.getStatut());
        List<GrantedAuthority> authorities = u.getRoles() == null
                ? List.of()
                : u.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getCode()))
                    .collect(Collectors.toList());

        return User.withUsername(u.getEmail())
                .password(u.getMotDePasse())
                .authorities(authorities)
                .disabled(!actif)
                .build();
    }
}
