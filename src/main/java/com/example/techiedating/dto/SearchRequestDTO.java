package com.example.techiedating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDTO {
    private String query; // General search query (name, bio, etc.)
    private List<Long> skillIds; // Filter by skills
    private Integer minExperience; // Minimum years of experience
    private Integer maxExperience; // Maximum years of experience
    private String gender; // Filter by gender
    private Double maxDistanceKm; // Maximum distance in kilometers
    private Double latitude; // Current user's latitude for distance calculation
    private Double longitude; // Current user's longitude for distance calculation
    private List<String> sortBy; // Fields to sort by (e.g., ["experienceYrs,desc", "displayName,asc"])
    private Integer page; // Page number (0-based)
    private Integer size; // Page size
}
