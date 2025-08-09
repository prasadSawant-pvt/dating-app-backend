package com.example.techiedating.controller;

import com.example.techiedating.dto.LocationDTO;
import com.example.techiedating.dto.locationiq.LocationIQResponse;
import com.example.techiedating.service.ILocationIQService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final ILocationIQService locationIQService;

    @GetMapping("/geocode")
    public ResponseEntity<LocationDTO> forwardGeocode(
            @RequestParam("query") String query) {
        
        try {
            LocationIQResponse response = locationIQService.geocodeAddress(query);
            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setLatitude(Double.parseDouble(response.getLatitude()));
            locationDTO.setLongitude(Double.parseDouble(response.getLongitude()));
            locationDTO.setDisplayName(response.getDisplayName());
            return ResponseEntity.ok(locationDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reverse")
    public ResponseEntity<LocationDTO> reverseGeocode(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude) {
        
        try {
            LocationIQResponse response = locationIQService.reverseGeocode(latitude, longitude);
            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setLatitude(Double.parseDouble(response.getLatitude()));
            locationDTO.setLongitude(Double.parseDouble(response.getLongitude()));
            locationDTO.setDisplayName(response.getDisplayName());
            return ResponseEntity.ok(locationDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
