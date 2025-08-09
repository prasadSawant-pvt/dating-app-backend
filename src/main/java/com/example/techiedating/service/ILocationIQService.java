package com.example.techiedating.service;

import com.example.techiedating.dto.locationiq.LocationIQResponse;

/**
 * Interface for geocoding and reverse geocoding operations.
 */
public interface ILocationIQService {
    
    /**
     * Geocode an address to get its coordinates
     * @param address The address to geocode
     * @return LocationIQResponse containing the coordinates and address information
     */
    LocationIQResponse geocodeAddress(String address);
    
    /**
     * Reverse geocode coordinates to get an address
     * @param lat The latitude
     * @param lon The longitude
     * @return LocationIQResponse containing the address information
     */
    LocationIQResponse reverseGeocode(double lat, double lon);
}
