package com.example.techiedating.dto;

import com.example.techiedating.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    private String bio;
    
    @NotNull(message = "Gender is required")
    private Gender gender;
    
    @PastOrPresent(message = "Date of birth must be in the past or present")
    private LocalDate dateOfBirth;
    
    private Double latitude;
    private Double longitude;
    
    @PositiveOrZero(message = "Experience years must be zero or positive")
    private Integer experienceYrs;
    
    private Set<String> interests;
}
