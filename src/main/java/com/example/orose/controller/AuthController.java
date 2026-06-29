package com.example.orose.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sert la page de connexion. Le POST /login est géré directement par
 * Spring Security (UsernamePasswordAuthenticationFilter), pas par ce controller.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
