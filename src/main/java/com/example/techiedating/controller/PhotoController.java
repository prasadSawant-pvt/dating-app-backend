package com.example.techiedating.controller;

import com.example.techiedating.dto.PhotoDTO;
import com.example.techiedating.mapper.PhotoMapper;
import com.example.techiedating.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final CloudinaryService cloudinaryService;
    private final PhotoMapper photoMapper = PhotoMapper.INSTANCE;

    @PostMapping
    public ResponseEntity<PhotoDTO> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        
        String userId = userDetails.getUsername();
        var photo = cloudinaryService.uploadProfilePhoto(file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(photoMapper.toPhotoDTO(photo));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable String photoId,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        
        cloudinaryService.deletePhoto(photoId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{photoId}/primary")
    public ResponseEntity<PhotoDTO> setPrimaryPhoto(
            @PathVariable String photoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        var photo = cloudinaryService.setPrimaryPhoto(photoId, userDetails.getUsername());
        return ResponseEntity.ok(photoMapper.toPhotoDTO(photo));
    }

    @GetMapping
    public ResponseEntity<List<PhotoDTO>> getUserPhotos(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        var photos = cloudinaryService.getUserPhotos(userDetails.getUsername());
        var photoDTOs = photos.stream()
                .map(photoMapper::toPhotoDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(photoDTOs);
    }
}
