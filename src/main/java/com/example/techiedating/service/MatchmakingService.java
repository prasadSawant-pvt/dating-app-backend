package com.example.techiedating.service;

import com.example.techiedating.dto.MatchScoreDTO;
import com.example.techiedating.exception.ProfileNotFoundException;
import com.example.techiedating.model.User;
import com.example.techiedating.model.UserProfile;
import com.example.techiedating.model.UserSkill;
import com.example.techiedating.repository.UserProfileRepository;
import com.example.techiedating.repository.UserRepository;
import com.example.techiedating.repository.UserSkillRepository;
import com.example.techiedating.service.cache.MatchmakingCacheService;
import com.example.techiedating.service.distance.DistanceCalculationService;
import com.example.techiedating.service.scoring.MatchScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling matchmaking operations between users.
 * Delegates specific responsibilities to specialized services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final MatchScoringService matchScoringService;
    private final MatchmakingCacheService cacheService;
    private final DistanceCalculationService distanceCalculationService;

    /**
     * Find potential matches for a user
     * @param username The username of the user to find matches for
     * @param page Page number (0-based)
     * @param size Number of matches per page
     * @return List of potential matches with scores
     */
    @Transactional(readOnly = true)
    public List<MatchScoreDTO> findPotentialMatches(String username, int page, int size) {
        log.info("Finding potential matches for user: {}, page: {}, size: {}", username, page, size);

        // Validate pagination parameters
        validatePagination(page, size);

        // Check cache first
        List<MatchScoreDTO> cachedResult = cacheService.getCachedMatches(username, page, size);
        if (cachedResult != null) {
            log.debug("Cache hit for matches: user={}, page={}, size={}", username, page, size);
            return cachedResult;
        }

        // Get current user and profile with validation
        User user = getUserByUsername(username);
        UserProfile currentUserProfile = getUserProfile(user);
        validateProfileCompleteness(currentUserProfile);

        // Get paginated potential matches
        List<UserProfile> potentialMatches = getPaginatedPotentialMatches(user.getId(), page, size);
        if (potentialMatches.isEmpty()) {
            return Collections.emptyList();
        }

        // Get current user's skills for comparison
        Map<Integer, Integer> currentUserSkills = getUserSkills(user.getId());

        // Calculate and return match scores
        return calculateMatchScores(currentUserProfile, potentialMatches, currentUserSkills);
    }

    /**
     * Calculate match score between two profiles
     * @param currentUser The current user's profile
     * @param otherUser The other user's profile to score
     * @param currentUserSkills Map of skill IDs to levels for the current user
     * @param skillNameCache Optional cache for skill names to reduce database lookups
     * @return MatchScoreDTO containing match details
     */
    @Transactional(readOnly = true)
    public MatchScoreDTO calculateMatchScore(UserProfile currentUser, UserProfile otherUser,
                                           Map<Integer, Integer> currentUserSkills,
                                           Map<Long, String> skillNameCache) {
        if (currentUser == null || otherUser == null) {
            throw new IllegalArgumentException("Both user profiles must be provided");
        }

        // Check cache first
        MatchScoreDTO cachedScore = cacheService.getCachedMatchScore(currentUser, otherUser);
        if (cachedScore != null) {
            return cachedScore;
        }

        // Get other user's skills
        List<UserSkill> otherUserSkills = userSkillRepository.findByUserId(otherUser.getUser().getId());

        // Delegate score calculation to MatchScoringService
        double totalScore = matchScoringService.calculateMatchScore(
                currentUser, 
                otherUser, 
                new LinkedHashMap<>(currentUserSkills), 
                otherUserSkills
        );

        // Get common skills with names
        List<String> commonSkills = getCommonSkills(
                currentUserSkills, 
                otherUserSkills,
                skillNameCache != null ? skillNameCache : new HashMap<>()
        );

        // Calculate distance
        double distance = distanceCalculationService.calculateDistance(
                currentUser.getLatitude(), 
                currentUser.getLongitude(),
                otherUser.getLatitude(), 
                otherUser.getLongitude()
        );

        // Build and return the result
        return MatchScoreDTO.builder()
                .userId(otherUser.getUser().getId())
                .displayName(otherUser.getDisplayName())
                .bio(otherUser.getBio())
                .gender(String.valueOf(otherUser.getGender()))
                .experienceYrs(otherUser.getExperienceYrs())
                .distanceKm(distance)
                .score(totalScore)
                .commonSkills(commonSkills)
                .build();
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must not be less than zero");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be at least 1");
        }
    }
    
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("User not found with username: " + username));
    }
    
    private UserProfile getUserProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + user.getUsername()));
    }
    
    private void validateProfileCompleteness(UserProfile profile) {
        if (profile.getGender() == null) {
            throw new ProfileNotFoundException("Please complete your profile by setting your gender");
        }
        if (profile.getInterests() == null || profile.getInterests().isEmpty()) {
            throw new ProfileNotFoundException("Please add some interests to your profile");
        }
    }
    
    private List<UserProfile> getPaginatedPotentialMatches(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "user.id"));
        return userProfileRepository.findByUserIdNot(userId, pageable).getContent();
    }
    
    private Map<Integer, Integer> getUserSkills(String userId) {
        return userSkillRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(
                        userSkill -> userSkill.getSkill().getId(),
                        UserSkill::getLevel,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
    
    private List<MatchScoreDTO> calculateMatchScores(UserProfile currentUser, 
                                                   List<UserProfile> potentialMatches,
                                                   Map<Integer, Integer> currentUserSkills) {
        return potentialMatches.stream()
                .map(profile -> calculateMatchScore(currentUser, profile, currentUserSkills, null))
                .sorted(Comparator.comparingDouble(MatchScoreDTO::getScore).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Extract common skill names between current user and another user
     * @param currentUserSkills Map of skill IDs to levels for current user
     * @param otherUserSkills List of skills for the other user
     * @param skillNameCache Cache for skill names to reduce DB lookups
     * @return List of common skill names
     */
    private List<String> getCommonSkills(
            Map<Integer, Integer> currentUserSkills,
            List<UserSkill> otherUserSkills,
            Map<Long, String> skillNameCache) {
        
        return otherUserSkills.stream()
                .filter(skill -> currentUserSkills.containsKey(skill.getSkill().getId()))
                .map(skill -> skillNameCache.computeIfAbsent(
                        Long.valueOf(skill.getSkill().getId()),
                        k -> skill.getSkill().getName()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Clear cache when user data changes
     */
    @Caching(evict = {
        @CacheEvict(value = "matches", allEntries = true),
        @CacheEvict(value = "matchScore", key = "#userId + '*'", allEntries = true)
    })
    public void clearUserMatchCache(String userId) {
        log.info("Clearing match cache for user: {}", userId);
    }
    
    /**
     * Scheduled cache eviction (runs every hour)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @CacheEvict(value = {"matches", "matchScore"}, allEntries = true)
    public void clearAllMatchCaches() {
        log.info("Scheduled cache eviction for all match caches");
    }
}
