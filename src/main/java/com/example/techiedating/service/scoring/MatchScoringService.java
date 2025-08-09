package com.example.techiedating.service.scoring;

import com.example.techiedating.model.UserProfile;
import com.example.techiedating.model.UserSkill;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating match scores between user profiles.
 */
public interface MatchScoringService {
    
    /**
     * Calculate match score between two user profiles
     * @param currentUser The current user's profile
     * @param otherUser The other user's profile to compare with
     * @param currentUserSkills Skills of the current user with their levels
     * @param otherUserSkills Skills of the other user with their levels
     * @return Match score between 0.0 and 1.0 (higher is better match)
     */
    double calculateMatchScore(
            UserProfile currentUser,
            UserProfile otherUser,
            LinkedHashMap<Integer, Integer> currentUserSkills,
            List<UserSkill> otherUserSkills
    );
    
    /**
     * Calculate skill compatibility score between two sets of skills
     * @param currentUserSkills Skills of the current user with their levels
     * @param otherUserSkills Skills of the other user with their levels
     * @return Score between 0.0 and 1.0
     */
    double calculateSkillScore(
            LinkedHashMap<Integer, Integer> currentUserSkills,
            List<UserSkill> otherUserSkills
    );
    
    /**
     * Calculate experience compatibility score
     * @param exp1 Experience in years of first user
     * @param exp2 Experience in years of second user
     * @return Score between 0.0 and 1.0
     */
    double calculateExperienceScore(Integer exp1, Integer exp2);
}
