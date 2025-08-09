package com.example.techiedating.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.techiedating.model.User;
import com.example.techiedating.model.Photo;
import com.example.techiedating.repository.PhotoRepository;
import com.example.techiedating.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    @Transactional
    public Photo uploadProfilePhoto(MultipartFile file, String username) throws IOException {
        // Find the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
                
        // Upload file to Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "dating-app/users/" + user.getId(),
                        "public_id", UUID.randomUUID().toString()
                )
        );

        // Create and save photo entity
        Photo photo = Photo.builder()
                .user(user)
                .url((String) uploadResult.get("secure_url"))
                .isPrimary(false) // Will be set to true if first photo
                .build();
        
        // If this is the first photo, set it as primary
        if (photoRepository.countByUser(user) == 0) {
            photo.setPrimary(true);
        }
        
        return photoRepository.save(photo);
    }

    @Transactional
    public void deletePhoto(String photoId, String userName) throws IOException {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + userName));
                
        Photo photo = photoRepository.findByIdAndUser(photoId, user)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found or access denied"));

        // Delete from Cloudinary
        String publicId = extractPublicId(photo.getUrl());
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }

        boolean wasPrimary = photo.isPrimary();
        
        // Delete from database
        photoRepository.delete(photo);

        // If we deleted the primary photo, set another one as primary if available
        if (wasPrimary) {
            photoRepository.findFirstByUser(user)
                    .ifPresent(p -> {
                        p.setPrimary(true);
                        photoRepository.save(p);
                    });
        }
    }

    @Transactional
    public Photo setPrimaryPhoto(String photoId, String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + userName));
                
        Photo newPrimary = photoRepository.findByIdAndUser(photoId, user)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found or access denied"));

        // Reset current primary photo if exists
        photoRepository.findByUserAndIsPrimaryTrue(user)
                .ifPresent(currentPrimary -> {
                    if (!currentPrimary.getId().equals(photoId)) {
                        currentPrimary.setPrimary(false);
                        photoRepository.save(currentPrimary);
                    }
                });

        // Set new primary photo and save
        newPrimary.setPrimary(true);
        Photo savedPhoto = photoRepository.save(newPrimary);
        return savedPhoto;
    }

    /**
     * Get all photos for a user
     * @param userId The ID of the user
     * @return List of photos
     */
    @Transactional(readOnly = true)
    public List<Photo> getUserPhotos(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + userName));
//        User user = new User();
//        user.setId(userId);
        return photoRepository.findByUser(user);
    }

    private String extractPublicId(String url) {
        // Extract public ID from Cloudinary URL
        // Example: https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg
        // Public ID would be: demo/sample
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            String[] parts = url.split("upload/");
            if (parts.length < 2) return null;
            
            // Get everything after 'upload/' and before the file extension
            String path = parts[1];
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash == -1) return null;
            
            String folder = path.substring(0, lastSlash);
            String filename = path.substring(lastSlash + 1);
            
            // Remove file extension
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0) {
                filename = filename.substring(0, dotIndex);
            }
            
            return folder + "/" + filename;
        } catch (Exception e) {
            return null;
        }
    }
}
