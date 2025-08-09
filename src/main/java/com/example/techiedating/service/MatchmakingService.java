package com.example.techiedating.service;

import com.example.techiedating.dto.MatchScoreDTO;
import com.example.techiedating.model.UserProfile;
import com.example.techiedating.model.UserSkill;
import com.example.techiedating.repository.UserProfileRepository;
import com.example.techiedating.repository.SkillRepository;
import com.example.techiedating.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchmakingService {

    // Weights for different match factors (adjust as needed)
    private static final double SKILL_WEIGHT = 0.5;
    private static final double DISTANCE_WEIGHT = 0.3;
    private static final double EXPERIENCE_WEIGHT = 0.2;
    
    // Maximum distance in kilometers to consider for matching
    private static final double MAX_DISTANCE_KM = 100.0;
    
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final PhotoService photoService;

    /**
     * Find potential matches for a user
     * @param currentUserId The ID of the user to find matches for
     * @param page Page number (0-based)
     * @param size Number of matches per page
     * @return List of potential matches with scores
     */
    @Cacheable(value = "matches", key = "#currentUserId + '_' + #page + '_' + #size")
    public List<MatchScoreDTO> findPotentialMatches(String currentUserId, int page, int size) {
        log.info("Calculating potential matches for user: {}, page: {}, size: {}", currentUserId, page, size);
        
        // Get current user's profile
        UserProfile currentUserUserProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user profile not found"));

        // Get all other profiles (paginated)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<UserProfile> potentialMatches = userProfileRepository.findByUserIdNot(currentUserId, pageable).getContent();

        // Get current user's skills for comparison
        LinkedHashMap<Integer, Integer> currentUserSkills = userSkillRepository.findByUserId(currentUserId).stream()
                .collect(Collectors.toMap(
                        userSkill -> userSkill.getSkill().getId(),
                        UserSkill::getLevel,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        // Calculate match scores for each potential match
        return potentialMatches.stream()
                .map(profile -> calculateMatchScore(currentUserUserProfile, profile, currentUserSkills, null))
                .sorted(Comparator.comparingDouble(MatchScoreDTO::getScore).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate match score between two profiles
     */
    @Cacheable(value = "matchScore", key = "#currentUser.user.id + '_' + #otherUser.user.id")
    public MatchScoreDTO calculateMatchScore(UserProfile currentUser, UserProfile otherUser,
                                             LinkedHashMap<Integer, Integer> currentUserSkills,
                                             Map<Long, String> skillNameCache) {
        
        // 1. Calculate skill compatibility (0.0 to 1.0)
        List<UserSkill> otherUserSkills = userSkillRepository.findByUserId(otherUser.getUser().getId());
        double skillScore = calculateSkillScore(currentUserSkills, otherUserSkills);
        
        // 2. Calculate distance score (0.0 to 1.0, with 1.0 being closest)
        double distance = calculateDistance(
                currentUser.getLatitude(), currentUser.getLongitude(),
                otherUser.getLatitude(), otherUser.getLongitude()
        );
        double distanceScore = 1.0 - Math.min(distance / MAX_DISTANCE_KM, 1.0);
        
        // 3. Calculate experience score (0.0 to 1.0, with 1.0 being same experience)
        double expScore = calculateExperienceScore(
                currentUser.getExperienceYrs(),
                otherUser.getExperienceYrs()
        );
        
        // 4. Calculate weighted total score (0.0 to 1.0)
        double totalScore = (skillScore * SKILL_WEIGHT) + 
                          (distanceScore * DISTANCE_WEIGHT) + 
                          (expScore * EXPERIENCE_WEIGHT);
        
        // 5. Get common skills
        List<String> commonSkills = otherUserSkills.stream()
                .filter(skill -> currentUserSkills.containsKey(skill.getSkill().getId()))
                .map(skill -> skill.getSkill().getName())
                .collect(Collectors.toList());
        
        // 6. Get primary photo URL if available
        String photoUrl = photoService.getPrimaryPhotoUrl(otherUser.getUser().getId());
        
        return MatchScoreDTO.builder()
                .userId(otherUser.getUser().getId())
                .displayName(otherUser.getDisplayName())
                .bio(otherUser.getBio())
                .gender(String.valueOf(otherUser.getGender()))
                .experienceYrs(otherUser.getExperienceYrs())
                .distanceKm(distance)
                .score(totalScore)
                .commonSkills(commonSkills)
                .photoUrl(photoUrl)
                .build();
    }
    
    /**
     * Calculate skill compatibility score (0.0 to 1.0)
     */
    private double calculateSkillScore(
            LinkedHashMap<Integer, Integer> currentUserSkills,
            List<UserSkill> otherUserSkills) {
        
        if (currentUserSkills.isEmpty() || otherUserSkills.isEmpty()) {
            return 0.0;
        }
        
        // Calculate total possible skill points
        double totalPossible = currentUserSkills.values().stream()
                .mapToInt(level -> level * level) // Square to give more weight to higher levels
                .sum();
        
        if (totalPossible == 0) {
            return 0.0;
        }
        
        // Calculate matching skill points
        double matchingPoints = otherUserSkills.stream()
                .filter(skill -> currentUserSkills.containsKey(skill.getSkill().getId()))
                .mapToInt(skill -> {
                    int currentLevel = currentUserSkills.get(skill.getSkill().getId());
                    return currentLevel * skill.getLevel(); // Multiply skill levels
                })
                .sum();
        
        // Normalize to 0.0-1.0 range
        return Math.min(matchingPoints / totalPossible, 1.0);
    }
    
    /**
     * Calculate distance between two points in kilometers using Haversine formula
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE; // Return max distance if location is not set
        }
        
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
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
    
    /**
     * Calculate experience compatibility score (0.0 to 1.0)
     */
    private double calculateExperienceScore(Integer exp1, Integer exp2) {
        if (exp1 == null || exp2 == null) {
            return 0.5; // Neutral score if experience is not set for either user
        }
        
        // Normalize experience difference to 0.0-1.0 range
        // The closer the experience, the higher the score
        double diff = Math.abs(exp1 - exp2);
        return Math.max(0, 1.0 - (diff / 20.0)); // Full score if same experience, decreases with difference
    }
}
