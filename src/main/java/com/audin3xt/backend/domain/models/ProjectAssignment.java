package com.audin3xt.backend.domain.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "project_assignments")
public class ProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_assignments_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    // Getters y Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
}