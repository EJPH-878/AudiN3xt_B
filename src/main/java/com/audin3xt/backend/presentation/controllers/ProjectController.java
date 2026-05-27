package com.audin3xt.backend.presentation.controllers;

import com.audin3xt.backend.domain.models.*;
import com.audin3xt.backend.domain.repositories.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAssignmentRepository assignmentRepository;

    // Ajustamos el constructor para inyectar los 3 repositorios necesarios
    public ProjectController(ProjectRepository projectRepository, 
                             UserRepository userRepository, 
                             ProjectAssignmentRepository assignmentRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // --- AQUÍ ESTÁ LA NUEVA LÓGICA DE AISLAMIENTO DE DATOS ---
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(Authentication authentication) {
        
        // 1. Extraemos el correo del usuario directamente del token (cookie)
        String userEmail = authentication.getName();
        
        // 2. Verificamos si el usuario actual tiene el rol de ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        // 3. Filtramos la respuesta en base al rol
        if (isAdmin) {
            // Si es ADMIN, le devolvemos la lista completa
            return ResponseEntity.ok(projectRepository.findAll());
        } else {
            // Si es AUDITOR, usamos la nueva consulta para traer solo lo suyo
            return ResponseEntity.ok(projectRepository.findProjectsByAuditorEmail(userEmail));
        }
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectRepository.save(project);
        return ResponseEntity.ok(savedProject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable UUID id, @RequestBody Project projectDetails) {
        Optional<Project> optionalProject = projectRepository.findById(id);
        
        if (optionalProject.isPresent()) {
            Project existingProject = optionalProject.get();
            existingProject.setName(projectDetails.getName());
            existingProject.setStatus(projectDetails.getStatus());
            
            if (projectDetails.getClient() != null) {
                existingProject.setClient(projectDetails.getClient());
            }
            
            Project updatedProject = projectRepository.save(existingProject);
            return ResponseEntity.ok(updatedProject);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable UUID id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Asignación de roles.
    @PutMapping("/{id}/assign")
    @Transactional
    public ResponseEntity<?> assignAuditors(@PathVariable UUID id, @RequestBody List<UUID> userIds) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        // Limpiamos asignaciones previas para evitar duplicados
        assignmentRepository.deleteByProjectId(id);
        
        // Asignamos a cada usuario al proyecto
        for (UUID userId : userIds) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            ProjectAssignment assignment = new ProjectAssignment();
            assignment.setProject(project);
            assignment.setUser(user);
            assignmentRepository.save(assignment);
        }
        
        return ResponseEntity.ok().build();
    }
}