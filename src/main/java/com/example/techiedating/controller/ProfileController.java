package com.example.techiedating.controller;

import com.example.techiedating.model.UserProfile;
import com.example.techiedating.repository.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class ProfileController {

    private final UserProfileRepository userProfileRepository;

    public ProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        // Stub: return first profile or empty
        Optional<UserProfile> p = userProfileRepository.findAll().stream().findFirst();
        return ResponseEntity.ok(p.orElseGet(() -> UserProfile.builder().displayName("NoProfile").build()));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfile userProfile) {
        // In real app, associate with authenticated user
        userProfileRepository.save(userProfile);
        return ResponseEntity.ok(userProfile);
    }
}
