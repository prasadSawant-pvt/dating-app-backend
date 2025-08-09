package com.example.techiedating.service;

import com.example.techiedating.model.Photo;
import com.example.techiedating.repository.PhotoRepository;
import com.example.techiedating.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoService {

    private final PhotoRepository photoRepository;

    /**
     * Get the URL of the primary photo for a user
     * @param userId The ID of the user
     * @return URL of the primary photo, or null if no primary photo exists
     */
    public String getPrimaryPhotoUrl(String userId) {
        return photoRepository.findByUserIdAndIsPrimary(userId, true)
                .map(Photo::getUrl)
                .orElse(null);
    }

    /**
     * Get all photos for a user
     * @param userId The ID of the user
     * @return List of photos
     */
    public List<Photo> getUserPhotos(String userId) {
        return photoRepository.findByUserId(userId);
    }

    /**
     * Set a photo as primary for a user
     * @param userId The ID of the user
     * @param photoId The ID of the photo to set as primary
     * @return true if the operation was successful, false otherwise
     */
    @Transactional
    public boolean setPrimaryPhoto(String userId, String photoId) {
        // Reset primary flag for all user's photos
        photoRepository.clearPrimaryFlag(userId);
        
        // Set the specified photo as primary
        int updated = photoRepository.setPrimaryFlag(photoId, userId);
        return updated > 0;
    }

    /**
     * Add a new photo for a user
     * @param userId The ID of the user
     * @param photoUrl The URL of the photo
     * @param isPrimary Whether this photo should be set as primary
     * @return The created photo
     */
    @Transactional
    public Photo addPhoto(String userId, String photoUrl, boolean isPrimary) {
        if (isPrimary) {
            // If this is a primary photo, clear any existing primary photo
            photoRepository.clearPrimaryFlag(userId);
        }
        
        // Create a minimal User with just the ID
        User user = new User();
        user.setId(userId);
        
        Photo photo = new Photo();
        photo.setUser(user);
        photo.setUrl(photoUrl);
        photo.setPrimary(isPrimary);
        
        return photoRepository.save(photo);
    }

    /**
     * Delete a photo
     * @param userId The ID of the user who owns the photo
     * @param photoId The ID of the photo to delete
     * @return true if the photo was deleted, false otherwise
     */
    @Transactional
    public boolean deletePhoto(String userId, String photoId) {
        return photoRepository.deleteByIdAndUserId(photoId, userId) > 0;
    }
}
