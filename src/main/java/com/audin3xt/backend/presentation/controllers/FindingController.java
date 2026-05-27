package com.audin3xt.backend.presentation.controllers;

import com.audin3xt.backend.domain.models.Finding;
import com.audin3xt.backend.domain.repositories.FindingRepository;
import com.audin3xt.backend.domain.repositories.UserRepository;
import com.audin3xt.backend.domain.repositories.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/findings")
public class FindingController {

    private final FindingRepository findingRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public FindingController(FindingRepository findingRepository, 
                             UserRepository userRepository,
                             ProjectRepository projectRepository) {
        this.findingRepository = findingRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    // MÉTODO ESTRELLA: Soporta filtros y paginación automáticamente
    @GetMapping
    public ResponseEntity<Page<Finding>> getAllFindings(
        @RequestParam(required = false) String severity,
        @RequestParam(required = false) String status,
        Pageable pageable
    ) {
        if (severity != null && !severity.isEmpty() && status != null && !status.isEmpty()) {
            return ResponseEntity.ok(findingRepository.findBySeverityAndStatus(severity, status, pageable));
        } else if (severity != null && !severity.isEmpty()) {
            return ResponseEntity.ok(findingRepository.findBySeverity(severity, pageable));
        } else if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(findingRepository.findByStatus(status, pageable));
        } else {
            return ResponseEntity.ok(findingRepository.findAll(pageable));
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Iterable<Finding>> getFindingsByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(findingRepository.findByProjectId(projectId));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Finding> createFinding(@RequestBody Finding finding) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userRepository.findByEmail(email).map(auditor -> {
            return projectRepository.findById(finding.getProject().getId()).map(project -> {
                finding.setAuthor(auditor);
                finding.setProject(project);
                
                Finding savedFinding = findingRepository.save(finding);
                return ResponseEntity.ok(savedFinding);
            }).orElse(ResponseEntity.badRequest().build()); 
        }).orElse(ResponseEntity.status(401).build()); 
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Finding> updateFinding(@PathVariable UUID id, @RequestBody Finding findingDetails) {
        Optional<Finding> optionalFinding = findingRepository.findById(id);
        
        if (optionalFinding.isPresent()) {
            Finding existingFinding = optionalFinding.get();
            existingFinding.setTitle(findingDetails.getTitle());
            existingFinding.setDescription(findingDetails.getDescription());
            existingFinding.setSeverity(findingDetails.getSeverity());
            existingFinding.setStatus(findingDetails.getStatus());
            
            if (findingDetails.getProject() != null) {
                existingFinding.setProject(findingDetails.getProject());
            }
            
            Finding updatedFinding = findingRepository.save(existingFinding);
            return ResponseEntity.ok(updatedFinding);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteFinding(@PathVariable UUID id) {
        if (findingRepository.existsById(id)) {
            findingRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}