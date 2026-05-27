package com.audin3xt.backend.domain.repositories;

import com.audin3xt.backend.domain.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    long countByStatusIgnoreCase(String status);

    // Consulta para obtener solo los proyectos asignados al correo del auditor
    @Query("SELECT pa.project FROM ProjectAssignment pa WHERE pa.user.email = :email")
    List<Project> findProjectsByAuditorEmail(@Param("email") String email);
}