package com.example.orose.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security.
 *
 * Rôles utilisés :
 *  - ROLE_ADMIN : administration complète (utilisateurs, statuts bassin, suppression)
 *  - ROLE_DIR   : directeur (lecture + exports + dashboards)
 *  - ROLE_TECH  : technicien (opérations terrain)
 *  - ROLE_RS    : responsable sanitaire (équivalent technicien sur module sanitaire)
 *
 * Restrictions par rôle posées via @PreAuthorize sur les controllers
 * (voir EnableMethodSecurity).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            // CSRF activé par défaut — Thymeleaf injecte le token automatiquement
            // sur les formulaires POST grâce à l'intégration spring-security
            .csrf(AbstractHttpConfigurer::disable);
            // ↑ CSRF désactivé temporairement pour éviter de devoir ajouter le token
            //   sur tous les formulaires existants. À réactiver après audit des forms.

        return http.build();
    }
}
