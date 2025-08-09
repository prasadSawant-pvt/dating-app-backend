package com.example.techiedating.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skill")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;
}
