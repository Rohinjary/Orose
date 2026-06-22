package com.example.orose.service;

import com.example.orose.dto.AlerteDTO;
import com.example.orose.model.Alerte;
import com.example.orose.repository.AlerteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlerteService {

    private final AlerteRepository alerteRepository;

    public AlerteService(AlerteRepository alerteRepository) {
        this.alerteRepository = alerteRepository;
    }

    public List<AlerteDTO> getAlertesDashboard() {
        return alerteRepository.findByEstResolueFalseOrderByDateCreationDesc()
            .stream()
            .map(a -> new AlerteDTO(a.getMessage(), a.getNiveau()))
            // Si vous utilisez une version de Java antérieure à 16, utilisez .collect(Collectors.toList())
            // sinon .toList() est parfait.
            .collect(Collectors.toList());
    }
}