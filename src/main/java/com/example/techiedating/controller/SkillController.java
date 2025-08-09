package com.example.techiedating.controller;

import com.example.techiedating.dto.UserSkillDTO;
import com.example.techiedating.dto.UserSkillRequest;
import com.example.techiedating.model.Skill;
import com.example.techiedating.repository.SkillRepository;
import com.example.techiedating.service.UserSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "APIs for managing skills and user skills")
public class SkillController {

    private final UserSkillService userSkillService;
    private final SkillRepository skillRepository;

    // Public endpoints for skills
    @GetMapping
    @Operation(summary = "Get all available skills")
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new skill (Admin only)")
    public ResponseEntity<Skill> createSkill(@Valid @RequestBody Skill skill) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillRepository.save(skill));
    }

    // User-specific skill endpoints (authenticated)
    @GetMapping("/me")
    @Operation(summary = "Get all skills for the authenticated user")
    public ResponseEntity<List<UserSkillDTO>> getUserSkills(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<UserSkillDTO> userSkills = userSkillService.getUserSkills(userDetails.getUsername());
        return ResponseEntity.ok(userSkills);
    }

    @PostMapping("/me")
    @Operation(summary = "Add a new skill to the authenticated user")
    public ResponseEntity<UserSkillDTO> addSkillToUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserSkillRequest request) {

        UserSkillDTO userSkill = userSkillService.addSkillToUser(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userSkill);
    }

    @PutMapping("/me/{userSkillId}")
    @Operation(summary = "Update a skill for the authenticated user")
    public ResponseEntity<UserSkillDTO> updateUserSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userSkillId,
            @Valid @RequestBody UserSkillRequest request) {

        UserSkillDTO updatedUserSkill = userSkillService.updateUserSkill(
                userDetails.getUsername(), userSkillId, request);
        return ResponseEntity.ok(updatedUserSkill);
    }

    @DeleteMapping("/me/{userSkillId}")
    @Operation(summary = "Remove a skill from the authenticated user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSkillFromUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userSkillId) {

        userSkillService.removeSkillFromUser(userDetails.getUsername(), userSkillId);
    }
}
