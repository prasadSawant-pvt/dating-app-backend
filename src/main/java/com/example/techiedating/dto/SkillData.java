package com.example.techiedating.dto;

import com.example.techiedating.model.SkillCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SkillData {
    private String name;
    private SkillCategory category;
    private String description;
    
    @JsonProperty("category")
    public void setCategory(String category) {
        try {
            this.category = SkillCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            this.category = SkillCategory.OTHER;
        }
    }
}
