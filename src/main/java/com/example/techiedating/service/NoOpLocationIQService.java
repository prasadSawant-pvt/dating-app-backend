package com.example.techiedating.service;

import com.example.techiedating.dto.locationiq.LocationIQResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * No-op implementation of ILocationIQService that will be used when LocationIQ is not configured.
 * This provides graceful degradation when the LocationIQ service is not available.
 */
@Slf4j
@Service
@Primary
public class NoOpLocationIQService implements ILocationIQService {
    
    @Override
    public LocationIQResponse geocodeAddress(String address) {
        log.warn("LocationIQ service is not configured. Geocoding is disabled.");
        return null;
    }
    
    @Override
    public LocationIQResponse reverseGeocode(double lat, double lon) {
        log.warn("LocationIQ service is not configured. Reverse geocoding is disabled.");
        return null;
    }
}
