package com.example.techiedating.dto.locationiq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationIQResponse {
    @JsonProperty("place_id")
    private String placeId;
    
    @JsonProperty("licence")
    private String licence;
    
    @JsonProperty("osm_type")
    private String osmType;
    
    @JsonProperty("osm_id")
    private String osmId;
    
    @JsonProperty("lat")
    private String latitude;
    
    @JsonProperty("lon")
    private String longitude;
    
    @JsonProperty("display_name")
    private String displayName;
    
    @JsonProperty("class")
    private String locationClass;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("importance")
    private Double importance;
    
    @JsonProperty("icon")
    private String iconUrl;
}
