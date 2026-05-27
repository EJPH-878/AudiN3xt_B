package com.audin3xt.backend.domain.repositories;

import com.audin3xt.backend.domain.models.Finding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FindingRepository extends JpaRepository<Finding, UUID> {
    
    List<Finding> findByProjectId(UUID projectId);
    
    // 1. Filtrado por severidad y estado (combinados)
    Page<Finding> findBySeverityAndStatus(String severity, String status, Pageable pageable);
    
    // 2. Filtrado individual
    Page<Finding> findBySeverity(String severity, Pageable pageable);
    Page<Finding> findByStatus(String status, Pageable pageable);
    
    // paginación general sin filtros
    Page<Finding> findAll(Pageable pageable);
}