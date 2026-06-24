package com.example.orose.controller;

import com.example.orose.dto.CreerDistributionDTO;
import com.example.orose.dto.NourrissageDashboardDTO;
import com.example.orose.dto.ValiderDistributionDTO;
import com.example.orose.model.DistributionNourriture;
import com.example.orose.service.NourrissageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Resource> dashboardPage() {
        Resource resource = new ClassPathResource("static/nourrissage/dashboard.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @PostMapping("/creer-distribution")
    @ResponseBody
    public ResponseEntity<DistributionNourriture> creerDistribution(@RequestBody CreerDistributionDTO dto) {
        DistributionNourriture distribution = nourrissageService.creerDistribution(dto);
        return ResponseEntity.ok(distribution);
    }

    @PostMapping("/valider-distribution")
    @ResponseBody
    public ResponseEntity<DistributionNourriture> validerDistribution(@RequestBody ValiderDistributionDTO dto) {
        DistributionNourriture distribution = nourrissageService.validerDistribution(dto);
        return ResponseEntity.ok(distribution);
    }
}
