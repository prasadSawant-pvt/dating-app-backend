package com.example.techiedating.service.cache;

import com.example.techiedating.dto.MatchScoreDTO;
import com.example.techiedating.model.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for managing matchmaking-related caching operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingCacheService {

    /**
     * Get cached matches for a user
     */
    @Cacheable(value = "matches", key = "#username + '_' + #page + '_' + #size")
    public List<MatchScoreDTO> getCachedMatches(String username, int page, int size) {
        log.debug("Cache miss for matches: user={}, page={}, size={}", username, page, size);
        return null; // Return null to indicate cache miss
    }

    /**
     * Cache match score calculation
     */
    @Cacheable(value = "matchScore", key = "#currentUser.user.id + '_' + #otherUser.user.id")
    public MatchScoreDTO getCachedMatchScore(UserProfile currentUser, UserProfile otherUser) {
        log.debug("Cache miss for match score: {} -> {}", 
                currentUser.getUser().getId(), otherUser.getUser().getId());
        return null; // Return null to indicate cache miss
    }

    /**
     * Clear cache for a specific user
     */
    @Caching(evict = {
        @CacheEvict(value = "matches", key = "#username + '*'", allEntries = true),
        @CacheEvict(value = "matchScore", key = "#username + '*'", allEntries = true)
    })
    public void clearUserCache(String username) {
        log.info("Clearing match cache for user: {}", username);
    }

    /**
     * Scheduled cache eviction (runs every hour)
     */
    @Scheduled(fixedRate = 3_600_000) // 1 hour in milliseconds
    @Caching(evict = {
        @CacheEvict(value = "matches", allEntries = true),
        @CacheEvict(value = "matchScore", allEntries = true)
    })
    public void clearAllCaches() {
        log.info("Scheduled cache eviction for all match caches");
    }
}
