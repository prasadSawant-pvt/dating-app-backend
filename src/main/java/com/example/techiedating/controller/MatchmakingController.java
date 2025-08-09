package com.example.techiedating.controller;

import com.example.techiedating.dto.MatchRequestDTO;
import com.example.techiedating.dto.MatchScoreDTO;
import com.example.techiedating.service.MatchmakingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    @GetMapping("/suggestions")
    public ResponseEntity<Page<MatchScoreDTO>> getMatchSuggestions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String currentUserId = userDetails.getUsername();
        List<MatchScoreDTO> matches = matchmakingService.findPotentialMatches(currentUserId, page, size);
        
        // Convert to Page for better pagination support
        Pageable pageable = PageRequest.of(page, size);
        Page<MatchScoreDTO> matchPage = new PageImpl<>(
                matches,
                pageable,
                matches.size()
        );
        
        return ResponseEntity.ok(matchPage);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<MatchScoreDTO>> searchMatches(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MatchRequestDTO request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String currentUserId = userDetails.getUsername();
        
        // For now, we'll use the same method as suggestions
        // In a real implementation, we would filter based on the request criteria
        List<MatchScoreDTO> matches = matchmakingService.findPotentialMatches(currentUserId, page, size);
        
        // Apply filters from request
        if (request.getMaxDistanceKm() != null) {
            matches = matches.stream()
                    .filter(m -> m.getDistanceKm() <= request.getMaxDistanceKm())
                    .toList();
        }
        
        if (request.getMinExperience() != null) {
            matches = matches.stream()
                    .filter(m -> m.getExperienceYrs() != null && m.getExperienceYrs() >= request.getMinExperience())
                    .toList();
        }
        
        if (request.getMaxExperience() != null) {
            matches = matches.stream()
                    .filter(m -> m.getExperienceYrs() != null && m.getExperienceYrs() <= request.getMaxExperience())
                    .toList();
        }
        
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            matches = matches.stream()
                    .filter(m -> request.getGender().equalsIgnoreCase(m.getGender()))
                    .toList();
        }
        
        // Convert to Page for better pagination support
        Pageable pageable = PageRequest.of(page, size);
        Page<MatchScoreDTO> matchPage = new PageImpl<>(
                matches,
                pageable,
                matches.size()
        );
        
        return ResponseEntity.ok(matchPage);
    }
}
