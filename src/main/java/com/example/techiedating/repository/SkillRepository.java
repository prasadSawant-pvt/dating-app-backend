package com.example.techiedating.repository;

import com.example.techiedating.model.Skill;
import com.example.techiedating.model.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Integer> {
    
    /**
     * Check if a skill with the given name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find a skill by name
     */
    Optional<Skill> findByName(String name);
    
    /**
     * Find all skills by category
     * @param category The skill category to search for
     * @return List of skills in the specified category
     */
    List<Skill> findByCategory(SkillCategory category);
    
    /**
     * Find all skills by slug
     */
    Optional<Skill> findBySlug(String slug);
}
