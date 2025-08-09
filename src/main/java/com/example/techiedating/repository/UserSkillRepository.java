package com.example.techiedating.repository;

import com.example.techiedating.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    
    /**
     * Find all skills for a specific user
     */
    List<UserSkill> findByUserId(String userId);
    
    /**
     * Find skills for multiple users
     */
    @Query("SELECT us FROM UserSkill us WHERE us.user.id IN :userIds")
    List<UserSkill> findByUserIds(@Param("userIds") List<String> userIds);
    
    /**
     * Get a map of skill IDs to skill levels for a user
     */
    @Query("SELECT us.skill.id, us.level FROM UserSkill us WHERE us.user.id = :userId")
    List<Object[]> findSkillLevelsByUserId(@Param("userId") String userId);
    
    /**
     * Get a map of user IDs to their skill levels
     */
    @Query("SELECT us.user.id, us.skill.id, us.level FROM UserSkill us WHERE us.user.id IN :userIds")
    List<Object[]> findSkillLevelsByUserIds(@Param("userIds") List<String> userIds);
    
    /**
     * Find users with specific skills at a minimum level
     */
    @Query("SELECT DISTINCT us.user.id FROM UserSkill us " +
           "WHERE us.skill.id = :skillId AND us.level >= :minLevel")
    List<String> findUserIdsBySkillAndMinLevel(
            @Param("skillId") Long skillId,
            @Param("minLevel") int minLevel);
    
    /**
     * Delete all skills for a user
     */
    void deleteByUserId(String userId);
    
    /**
     * Check if a user has a specific skill
     */
    boolean existsByUserIdAndSkillId(String userId, Long skillId);
    
    /**
     * Get a user's skill level for a specific skill
     */
    @Query("SELECT us.level FROM UserSkill us WHERE us.user.id = :userId AND us.skill.id = :skillId")
    Integer findLevelByUserIdAndSkillId(
            @Param("userId") String userId,
            @Param("skillId") Long skillId);
    
    /**
     * Get a map of skill names to levels for a user
     */
    @Query("SELECT s.name, us.level FROM UserSkill us " +
           "JOIN us.skill s WHERE us.user.id = :userId")
    List<Object[]> findSkillNamesAndLevelsByUserId(@Param("userId") String userId);
}
