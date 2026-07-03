package com.example.orose.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.orose.service.AccueilService;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class DashboardController {

    private final AccueilService accueilService;

    @Autowired
    public DashboardController(AccueilService accueilService) {
        this.accueilService = accueilService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("bassinsActifs", accueilService.etatsTousBassinsActifs());
        model.addAttribute("situationStock", accueilService.getSituationStock());
        return "dashboard";
    }
}
