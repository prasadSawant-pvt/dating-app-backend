package com.example.techiedating.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category;

    @Column(unique = true, nullable = false)
    private String slug; // URL-friendly version of the name for lookups

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void generateSlug() {
        if (name != null) {
            this.slug = name.trim().toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                    .replaceAll("\\s+", "-")          // Replace spaces with hyphens
                    .replaceAll("-" + "+", "-");       // Replace multiple hyphens with single
        }
    }
}
