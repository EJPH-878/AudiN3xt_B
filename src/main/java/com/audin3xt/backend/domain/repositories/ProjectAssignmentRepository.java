package com.audin3xt.backend.domain.repositories;

import com.audin3xt.backend.domain.models.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, UUID> {
    
    // Método necesario para limpiar asignaciones previas
    @Modifying
    @Query("DELETE FROM ProjectAssignment pa WHERE pa.project.id = :projectId")
    void deleteByProjectId(UUID projectId);
}