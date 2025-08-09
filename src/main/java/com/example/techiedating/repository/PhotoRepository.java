package com.example.techiedating.repository;

import com.example.techiedating.model.Photo;
import com.example.techiedating.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, String> {
    // Find all photos by user
    List<Photo> findByUser(User user);
    
    // Find a specific photo by ID and user
    Optional<Photo> findByIdAndUser(String id, User user);
    
    // Find primary photo for a user
    Optional<Photo> findByUserAndIsPrimaryTrue(User user);
    
    // Find first photo for a user (for setting as primary when deleting current primary)
    Optional<Photo> findFirstByUser(User user);
    
    // Count photos for a user
    long countByUser(User user);
    
    // Check if a photo exists for a user
    boolean existsByUser(User user);
    
    // Clear primary flag for all photos of a user
    @Modifying
    @Query("UPDATE Photo p SET p.isPrimary = false WHERE p.user = :user")
    int clearPrimaryFlag(@Param("user") User user);
    
    // Set a photo as primary
    @Modifying
    @Query("UPDATE Photo p SET p.isPrimary = true WHERE p.id = :photoId AND p.user = :user")
    int setPrimaryFlag(@Param("photoId") String photoId, @Param("user") User user);
    
    // Delete a photo by ID and user
    @Modifying
    @Query("DELETE FROM Photo p WHERE p.id = :photoId AND p.user = :user")
    int deleteByIdAndUser(@Param("photoId") String photoId, @Param("user") User user);
    
    // Find all photos by user ID (for backward compatibility)
    @Query("SELECT p FROM Photo p WHERE p.user.id = :userId")
    List<Photo> findByUserId(@Param("userId") String userId);
    
    // Find a specific photo by ID and user ID (for backward compatibility)
    @Query("SELECT p FROM Photo p WHERE p.id = :id AND p.user.id = :userId")
    Optional<Photo> findByIdAndUserId(@Param("id") String id, @Param("userId") String userId);
    
    // Find primary photo by user ID (for backward compatibility)
    @Query("SELECT p FROM Photo p WHERE p.user.id = :userId AND p.isPrimary = true")
    Optional<Photo> findPrimaryByUserId(@Param("userId") String userId);
    
    // Find first photo by user ID (for backward compatibility)
    @Query("SELECT p FROM Photo p WHERE p.user.id = :userId ORDER BY p.createdAt ASC")
    Optional<Photo> findFirstByUserId(@Param("userId") String userId);
    
    // Check if a photo exists for a user ID (for backward compatibility)
    @Query("SELECT COUNT(p) > 0 FROM Photo p WHERE p.user.id = :userId")
    boolean existsByUserId(@Param("userId") String userId);
    
    // Find by user ID and primary status (for backward compatibility)
    @Query("SELECT p FROM Photo p WHERE p.user.id = :userId AND p.isPrimary = :isPrimary")
    Optional<Photo> findByUserIdAndIsPrimary(@Param("userId") String userId, @Param("isPrimary") boolean isPrimary);
    
    // Clear primary flag by user ID (for backward compatibility)
    @Modifying
    @Query("UPDATE Photo p SET p.isPrimary = false WHERE p.user.id = :userId")
    int clearPrimaryFlag(@Param("userId") String userId);
    
    // Set primary flag by photo ID and user ID (for backward compatibility)
    @Modifying
    @Query("UPDATE Photo p SET p.isPrimary = true WHERE p.id = :photoId AND p.user.id = :userId")
    int setPrimaryFlag(@Param("photoId") String photoId, @Param("userId") String userId);
    
    // Delete by ID and user ID (for backward compatibility)
    @Modifying
    @Query("DELETE FROM Photo p WHERE p.id = :photoId AND p.user.id = :userId")
    int deleteByIdAndUserId(@Param("photoId") String photoId, @Param("userId") String userId);
}
