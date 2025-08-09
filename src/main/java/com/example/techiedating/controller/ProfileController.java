package com.example.techiedating.controller;

import com.example.techiedating.dto.ProfileResponse;
import com.example.techiedating.model.User;
import com.example.techiedating.model.UserProfile;
import com.example.techiedating.repository.UserProfileRepository;
import com.example.techiedating.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
        
        if (profileOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "PROFILE_NOT_FOUND");
            response.put("message", "Profile not found. Please create your profile.");
            response.put("userId", user.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(convertToProfileResponse(profileOpt.get()));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileResponse profileUpdate) {
            
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElse(new UserProfile());
                
        // Update profile fields from DTO
        profile.setUser(user);
        profile.setDisplayName(profileUpdate.getDisplayName());
        profile.setBio(profileUpdate.getBio());
        profile.setGender(profileUpdate.getGender());
        profile.setDateOfBirth(profileUpdate.getDateOfBirth());
        profile.setLatitude(profileUpdate.getLatitude());
        profile.setLongitude(profileUpdate.getLongitude());
        profile.setExperienceYrs(profileUpdate.getExperienceYrs());
        
        if (profileUpdate.getInterests() != null) {
            profile.setInterests(profileUpdate.getInterests());
        }
        
        // Set user relationship
        profile.setUser(user);
        
        UserProfile savedProfile = userProfileRepository.save(profile);
        return ResponseEntity.ok(convertToProfileResponse(savedProfile));
    }
    
    private ProfileResponse convertToProfileResponse(UserProfile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .gender(profile.getGender())
                .dateOfBirth(profile.getDateOfBirth())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .experienceYrs(profile.getExperienceYrs())
                .interests(profile.getInterests())
                .build();
    }
}
