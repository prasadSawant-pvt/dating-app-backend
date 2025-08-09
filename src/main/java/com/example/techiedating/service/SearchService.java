package com.example.techiedating.service;

import com.example.techiedating.dto.MatchScoreDTO;
import com.example.techiedating.dto.SearchRequestDTO;
import com.example.techiedating.dto.SearchResultDTO;
import com.example.techiedating.model.UserProfile;
import com.example.techiedating.model.UserSkill;
import com.example.techiedating.repository.UserProfileRepository;
import com.example.techiedating.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final MatchmakingService matchmakingService;

    /**
     * Search for profiles based on the provided criteria
     */
    public SearchResultDTO<MatchScoreDTO> searchProfiles(SearchRequestDTO request, String currentUserId) {
        // Build pageable with sorting
        Pageable pageable = createPageable(request);
        
        // Get all profiles that match the basic criteria (excluding current user)
        Page<UserProfile> profiles = userProfileRepository.searchProfiles(
                currentUserId,
                request.getQuery() != null ? request.getQuery().trim() : null,
                request.getGender(),
                request.getMinExperience(),
                request.getMaxExperience(),
                pageable
        );
        
        // Convert to MatchScoreDTO with scores
        List<MatchScoreDTO> results = profiles.getContent().stream()
                .map(profile -> {
                    // Get user skills for skill-based filtering
                    LinkedHashMap<Integer, Integer> profileSkills = userSkillRepository.findByUserId(profile.getUser().getId()).stream()
                            .collect(Collectors.toMap(
                                    userSkill -> userSkill.getSkill().getId(),
                                    UserSkill::getLevel,
                                    (existing, replacement) -> existing,
                                    LinkedHashMap::new
                            ));
                    
                    // Get current user's skills for score calculation
                    LinkedHashMap<Integer, Integer> currentUserSkills = userSkillRepository.findByUserId(currentUserId).stream()
                            .collect(Collectors.toMap(
                                    userSkill -> userSkill.getSkill().getId(),
                                    UserSkill::getLevel,
                                    (existing, replacement) -> existing,
                                    LinkedHashMap::new
                            ));
                    
                    // Calculate match score
                    return matchmakingService.calculateMatchScore(
                            userProfileRepository.findByUserId(currentUserId)
                                    .orElseThrow(() -> new IllegalArgumentException("Current user profile not found")),
                            profile,
                            currentUserSkills,
                            null // We'll handle skill names separately if needed
                    );
                })
                // Apply skill filter if specified
                .filter(result -> request.getSkillIds() == null || request.getSkillIds().isEmpty() || 
                        result.getCommonSkills().stream()
                                .anyMatch(skill -> request.getSkillIds().contains(skill)))
                // Apply distance filter if specified
                .filter(result -> request.getMaxDistanceKm() == null || 
                        (result.getDistanceKm() != null && result.getDistanceKm() <= request.getMaxDistanceKm()))
                .collect(Collectors.toList());
        
        // Create a custom page for the filtered results
        Page<MatchScoreDTO> resultPage = createCustomPage(results, pageable, profiles.getTotalElements());
        
        return SearchResultDTO.fromPage(resultPage);
    }
    
    /**
     * Create a pageable with sorting based on the request
     */
    private Pageable createPageable(SearchRequestDTO request) {
        // Default sorting
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        
        // Apply custom sorting if specified
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            List<Sort.Order> orders = request.getSortBy().stream()
                    .map(sortParam -> {
                        String[] parts = sortParam.split(",");
                        String property = parts[0].trim();
                        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()) ? 
                                Sort.Direction.DESC : Sort.Direction.ASC;
                        return new Sort.Order(direction, property);
                    })
                    .collect(Collectors.toList());
            
            sort = Sort.by(orders);
        }
        
        // Default pagination if not specified
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        
        return PageRequest.of(page, size, sort);
    }
    
    /**
     * Create a custom page from a list of results
     */
    private <T> Page<T> createCustomPage(List<T> items, Pageable pageable, long total) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        
        List<T> pageContent;
        if (items.size() < startItem) {
            pageContent = List.of();
        } else {
            int toIndex = Math.min(startItem + pageSize, items.size());
            pageContent = items.subList(startItem, toIndex);
        }
        
        return new org.springframework.data.domain.PageImpl<>(
                pageContent, 
                pageable, 
                Math.min(items.size(), total)
        );
    }
}
