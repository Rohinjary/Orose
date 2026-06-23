package com.example.orose.controller;

import com.example.orose.dto.NourrissageDashboardDTO;
import com.example.orose.service.NourrissageService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/nourrissage")
public class NourrissageController {

    private final NourrissageService nourrissageService;

    public NourrissageController(NourrissageService nourrissageService) {
        this.nourrissageService = nourrissageService;
    }

    @GetMapping("/dash_nourri")
    @ResponseBody
    public ResponseEntity<NourrissageDashboardDTO> getDashboardNourrissage() {
        NourrissageDashboardDTO dashboard = nourrissageService.getDashboardNourrissage();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "redirect:/nourrissage/dashboard.html";
    }
}
