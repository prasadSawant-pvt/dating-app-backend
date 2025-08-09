package com.example.techiedating.service.scoring;

import com.example.techiedating.model.UserProfile;
import com.example.techiedating.model.UserSkill;
import com.example.techiedating.service.distance.DistanceCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of MatchScoringService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultMatchScoringService implements MatchScoringService {

    // Weights for different match factors
    private static final double SKILL_WEIGHT = 0.5;
    private static final double DISTANCE_WEIGHT = 0.3;
    private static final double EXPERIENCE_WEIGHT = 0.2;
    
    // Maximum distance in kilometers to consider for matching
    private static final double MAX_DISTANCE_KM = 100.0;
    
    private final DistanceCalculationService distanceCalculationService;

    @Override
    public double calculateMatchScore(
            UserProfile currentUser,
            UserProfile otherUser,
            LinkedHashMap<Integer, Integer> currentUserSkills,
            List<UserSkill> otherUserSkills) {
        
        // 1. Calculate skill compatibility
        double skillScore = calculateSkillScore(currentUserSkills, otherUserSkills);
        
        // 2. Calculate distance score
        double distance = distanceCalculationService.calculateDistance(
                currentUser.getLatitude(), currentUser.getLongitude(),
                otherUser.getLatitude(), otherUser.getLongitude()
        );
        double distanceScore = 1.0 - Math.min(distance / MAX_DISTANCE_KM, 1.0);
        
        // 3. Calculate experience score
        double expScore = calculateExperienceScore(
                currentUser.getExperienceYrs(),
                otherUser.getExperienceYrs()
        );
        
        // 4. Calculate weighted total score
        double totalScore = (skillScore * SKILL_WEIGHT) +
                          (distanceScore * DISTANCE_WEIGHT) +
                          (expScore * EXPERIENCE_WEIGHT);
        
        log.debug("Match score - Skills: {}, Distance: {}, Exp: {}, Total: {}", 
                skillScore, distanceScore, expScore, totalScore);
        
        return Math.min(Math.max(totalScore, 0.0), 1.0); // Ensure score is between 0.0 and 1.0
    }

    @Override
    public double calculateSkillScore(
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

    @Override
    public double calculateExperienceScore(Integer exp1, Integer exp2) {
        if (exp1 == null || exp2 == null) {
            return 0.5; // Neutral score if experience is not set for either user
        }
        
        // Normalize experience difference to 0.0-1.0 range
        // The closer the experience, the higher the score
        double diff = Math.abs(exp1 - exp2);
        return Math.max(0, 1.0 - (diff / 20.0)); // Full score if same experience, decreases with difference
    }
}
