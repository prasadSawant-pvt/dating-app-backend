package com.example.techiedating.controller;

import com.example.techiedating.dto.MatchScoreDTO;
import com.example.techiedating.dto.SearchRequestDTO;
import com.example.techiedating.dto.SearchResultDTO;
import com.example.techiedating.service.SearchService;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<SearchResultDTO<MatchScoreDTO>> searchProfiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SearchRequestDTO request) {
        
        String username = userDetails.getUsername();
        SearchResultDTO<MatchScoreDTO> result = searchService.searchProfiles(request, username);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<SearchResultDTO<MatchScoreDTO>> searchProfilesWithQueryParams(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Double maxDistanceKm,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) List<String> sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Build search request from query parameters
        SearchRequestDTO request = SearchRequestDTO.builder()
                .query(query)
                .skillIds(skillIds)
                .minExperience(minExperience)
                .maxExperience(maxExperience)
                .gender(gender)
                .maxDistanceKm(maxDistanceKm)
                .latitude(latitude)
                .longitude(longitude)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();
        
        String currentUserId = userDetails.getUsername();
        SearchResultDTO<MatchScoreDTO> result = searchService.searchProfiles(request, currentUserId);
        return ResponseEntity.ok(result);
    }
}
