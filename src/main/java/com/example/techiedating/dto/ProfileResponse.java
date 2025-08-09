package com.example.techiedating.dto;

import com.example.techiedating.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String displayName;
    private String bio;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Double latitude;
    private Double longitude;
    private Integer experienceYrs;
    private Set<String> interests;
}
