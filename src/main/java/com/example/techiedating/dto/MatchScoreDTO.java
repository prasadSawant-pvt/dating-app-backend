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
public class MatchScoreDTO {
    private String userId;
    private String displayName;
    private String bio;
    private String gender;
    private Integer experienceYrs;
    private Double distanceKm;
    private Double score;
    private List<String> commonSkills;
    private String photoUrl;
}
