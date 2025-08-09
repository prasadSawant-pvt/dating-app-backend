package com.example.techiedating.service;

import com.example.techiedating.config.LocationIQConfig;
import com.example.techiedating.dto.locationiq.LocationIQResponse;
import com.example.techiedating.exception.GeocodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Service for geocoding and reverse geocoding using LocationIQ API.
 * This implementation is used when LocationIQ is properly configured.
 */
@Slf4j
@ConditionalOnBean(LocationIQConfig.class)
@Service
@Primary
public class DefaultLocationIQService implements ILocationIQService {

    private final RestTemplate restTemplate;
    private final LocationIQConfig locationIQConfig;

    @Autowired
    public DefaultLocationIQService(RestTemplate restTemplate, LocationIQConfig locationIQConfig) {
        this.restTemplate = restTemplate;
        this.locationIQConfig = locationIQConfig;
    }

    @Override
    @Cacheable(value = "geocode", key = "#address")
    public LocationIQResponse geocodeAddress(String address) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(locationIQConfig.getUrl() + "/search")
                    .queryParam("key", locationIQConfig.getKey())
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("normalizeaddress", 1)
                    .build()
                    .toUri();

            ResponseEntity<LocationIQResponse[]> response = restTemplate.getForEntity(
                    uri, LocationIQResponse[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                return response.getBody()[0];
            }

            throw new GeocodingException("No results found for address: " + address);
        } catch (Exception e) {
            throw new GeocodingException("Error geocoding address: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "reverseGeocode", key = "#lat + ',' + #lon")
    public LocationIQResponse reverseGeocode(double lat, double lon) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(locationIQConfig.getUrl() + "/reverse")
                    .queryParam("key", locationIQConfig.getKey())
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("format", "json")
                    .queryParam("normalizeaddress", 1)
                    .build()
                    .toUri();

            return restTemplate.getForObject(uri, LocationIQResponse.class);
        } catch (Exception e) {
            throw new GeocodingException("Error in reverse geocoding: " + e.getMessage(), e);
        }
    }
}
