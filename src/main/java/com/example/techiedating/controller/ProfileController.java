package com.example.techiedating.controller;

import com.example.techiedating.dto.ProfileResponse;
import com.example.techiedating.dto.UpdateProfileRequest;
import com.example.techiedating.exception.ProfileNotFoundException;
import com.example.techiedating.model.User;
import com.example.techiedating.model.UserProfile;
import com.example.techiedating.repository.UserProfileRepository;
import com.example.techiedating.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + user.getUsername()));
        
        return ResponseEntity.ok(convertToProfileResponse(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest updateRequest) {
        
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("User not found with username: " + username));
                
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    // Create new profile if it doesn't exist
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    newProfile.setId(user.getId()); // Ensure ID matches user ID
                    return newProfile;
                });
                
        // Update profile fields from request
        profile.setDisplayName(updateRequest.getDisplayName());
        profile.setBio(updateRequest.getBio());
        profile.setGender(updateRequest.getGender());
        profile.setDateOfBirth(updateRequest.getDateOfBirth());
        profile.setLatitude(updateRequest.getLatitude());
        profile.setLongitude(updateRequest.getLongitude());
        profile.setExperienceYrs(updateRequest.getExperienceYrs());
        
        if (updateRequest.getInterests() != null) {
            profile.setInterests(new HashSet<>(updateRequest.getInterests()));
        }
        
        // Ensure the profile is linked to the user
        if (profile.getUser() == null) {
            profile.setUser(user);
        }
        
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
