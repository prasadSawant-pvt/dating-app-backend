package com.example.techiedating.repository;

import com.example.techiedating.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, String>, UserProfileRepositoryCustom {
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserId(@Param("userId") String userId);
    
    // Find all profiles except the current user with pagination
    @Query("SELECT up FROM UserProfile up WHERE up.user.id != :userId")
    Page<UserProfile> findByUserIdNot(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.email = :email")
    Optional<UserProfile> findByUserEmail(@Param("email") String email);
}
