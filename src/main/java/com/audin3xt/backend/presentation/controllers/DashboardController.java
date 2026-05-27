package com.audin3xt.backend.presentation.controllers;

import com.audin3xt.backend.domain.repositories.ProjectRepository; // Ajusta el paquete si tu repositorio está en otra carpeta
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
// CAMBIO APLICADO AQUÍ (y su importación arriba):
public class DashboardController {

    private final ProjectRepository projectRepository;

    public DashboardController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        
        // ACTUALIZA ESTA LÍNEA PARA USAR EL NUEVO MÉTODO:
        long activeProjects = projectRepository.countByStatusIgnoreCase("Activo"); 
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("activeProjects", activeProjects);
        
        return ResponseEntity.ok(stats);
    }
}