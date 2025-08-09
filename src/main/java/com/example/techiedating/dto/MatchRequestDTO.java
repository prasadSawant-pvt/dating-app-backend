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
public class MatchRequestDTO {
    private List<Long> skillIds; // Optional: Filter by specific skills
    private Double maxDistanceKm; // Optional: Maximum distance in kilometers
    private Integer minExperience; // Optional: Minimum years of experience
    private Integer maxExperience; // Optional: Maximum years of experience
    private String gender; // Optional: Filter by gender
}
