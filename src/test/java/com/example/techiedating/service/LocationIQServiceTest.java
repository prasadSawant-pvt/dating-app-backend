package com.example.techiedating.service;

import com.example.techiedating.config.LocationIQConfig;
import com.example.techiedating.dto.locationiq.LocationIQResponse;
import com.example.techiedating.exception.GeocodingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationIQServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LocationIQConfig locationIQConfig;

    @InjectMocks
    private LocationIQService locationIQService;

    @BeforeEach
    void setUp() {
        when(locationIQConfig.getUrl()).thenReturn("https://us1.locationiq.com/v1");
        when(locationIQConfig.getKey()).thenReturn("test-api-key");
    }

    @Test
    void geocodeAddress_ShouldReturnLocationIQResponse() {
        // Arrange
        String address = "1600 Amphitheatre Parkway, Mountain View, CA";
        LocationIQResponse mockResponse = new LocationIQResponse();
        mockResponse.setLatitude("37.4224");
        mockResponse.setLongitude("-122.0842");
        mockResponse.setDisplayName("Googleplex, 1600, Amphitheatre Parkway, Mountain View, Santa Clara County, California, 94043, United States");

        when(restTemplate.getForEntity(any(URI.class), eq(LocationIQResponse[].class)))
                .thenReturn(ResponseEntity.ok(new LocationIQResponse[]{mockResponse}));

        // Act
        LocationIQResponse response = locationIQService.geocodeAddress(address);

        // Assert
        assertNotNull(response);
        assertEquals("37.4224", response.getLatitude());
        assertEquals("-122.0842", response.getLongitude());
        assertTrue(response.getDisplayName().contains("Googleplex"));
    }

    @Test
    void geocodeAddress_ShouldThrowException_WhenNoResults() {
        // Arrange
        String address = "Nonexistent Address";
        when(restTemplate.getForEntity(any(URI.class), eq(LocationIQResponse[].class)))
                .thenReturn(ResponseEntity.ok(new LocationIQResponse[0]));

        // Act & Assert
        assertThrows(GeocodingException.class, () -> locationIQService.geocodeAddress(address));
    }

    @Test
    void reverseGeocode_ShouldReturnLocationIQResponse() {
        // Arrange
        double lat = 37.4224;
        double lon = -122.0842;
        LocationIQResponse mockResponse = new LocationIQResponse();
        mockResponse.setLatitude("37.4224");
        mockResponse.setLongitude("-122.0842");
        mockResponse.setDisplayName("Googleplex, 1600, Amphitheatre Parkway, Mountain View, Santa Clara County, California, 94043, United States");

        when(restTemplate.getForObject(any(URI.class), eq(LocationIQResponse.class)))
                .thenReturn(mockResponse);

        // Act
        LocationIQResponse response = locationIQService.reverseGeocode(lat, lon);

        // Assert
        assertNotNull(response);
        assertEquals("37.4224", response.getLatitude());
        assertEquals("-122.0842", response.getLongitude());
        assertTrue(response.getDisplayName().contains("Googleplex"));
    }
}
