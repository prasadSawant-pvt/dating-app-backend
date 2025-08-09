package com.example.techiedating.repository;

import com.example.techiedating.model.UserProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserProfileRepositoryCustomImpl extends SimpleJpaRepository<UserProfile, String> 
        implements UserProfileRepositoryCustom {

    private final EntityManager entityManager;

    public UserProfileRepositoryCustomImpl(EntityManager entityManager) {
        super(UserProfile.class, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public Page<UserProfile> searchProfiles(
            String currentUserId,
            String query,
            String gender,
            Integer minExperience,
            Integer maxExperience,
            Pageable pageable) {
        
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(UserProfile.class);
        var profile = cq.from(UserProfile.class);
        
        // Select distinct profiles
        cq.select(profile).distinct(true);
        
        // List to hold predicates
        List<Predicate> predicates = new ArrayList<>();
        
        // Exclude current user
        predicates.add(cb.notEqual(profile.get("id"), currentUserId));
        
        // Add search query predicate (search in displayName and bio)
        if (query != null && !query.isEmpty()) {
            String searchPattern = "%" + query.toLowerCase() + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(profile.get("displayName")), searchPattern),
                    cb.like(cb.lower(profile.get("bio")), searchPattern)
                )
            );
        }
        
        // Add gender filter
        if (gender != null && !gender.isEmpty()) {
            predicates.add(cb.equal(profile.get("gender"), gender));
        }
        
        // Add experience range filter
        if (minExperience != null) {
            predicates.add(cb.greaterThanOrEqualTo(profile.get("experienceYrs"), minExperience));
        }
        
        if (maxExperience != null) {
            predicates.add(cb.lessThanOrEqualTo(profile.get("experienceYrs"), maxExperience));
        }
        
        // Apply all predicates
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        
        // Execute query with pagination
        var typedQuery = entityManager.createQuery(cq);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        // Get the result list
        List<UserProfile> resultList = typedQuery.getResultList();
        
        // Create a separate count query with its own criteria builder and predicates
        var countCb = entityManager.getCriteriaBuilder();
        var countCq = countCb.createQuery(Long.class);
        var countProfile = countCq.from(UserProfile.class);
        countCq.select(countCb.count(countProfile));
        
        // Rebuild predicates for the count query to avoid path registration conflicts
        List<Predicate> countPredicates = new ArrayList<>();
        
        // Exclude current user
        countPredicates.add(countCb.notEqual(countProfile.get("id"), currentUserId));
        
        // Add search query predicate (search in displayName and bio)
        if (query != null && !query.isEmpty()) {
            String searchPattern = "%" + query.toLowerCase() + "%";
            countPredicates.add(
                countCb.or(
                    countCb.like(countCb.lower(countProfile.get("displayName")), searchPattern),
                    countCb.like(countCb.lower(countProfile.get("bio")), searchPattern)
                )
            );
        }
        
        // Add gender filter
        if (gender != null && !gender.isEmpty()) {
            countPredicates.add(countCb.equal(countProfile.get("gender"), gender));
        }
        
        // Add experience range filter
        if (minExperience != null) {
            countPredicates.add(countCb.greaterThanOrEqualTo(countProfile.get("experienceYrs"), minExperience));
        }
        
        if (maxExperience != null) {
            countPredicates.add(countCb.lessThanOrEqualTo(countProfile.get("experienceYrs"), maxExperience));
        }
        
        // Apply all predicates to count query
        if (!countPredicates.isEmpty()) {
            countCq.where(countPredicates.toArray(new Predicate[0]));
        }
        
        // Get the total count
        Long total = entityManager.createQuery(countCq).getSingleResult();
        
        return new PageImpl<>(resultList, pageable, total);
    }
}
