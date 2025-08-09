package com.example.techiedating.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "locationiq.api")
@ConditionalOnProperty(prefix = "locationiq.api", name = "key")
@Validated
public class LocationIQConfig {

    @NotBlank(message = "LocationIQ API key is required")
    private String key;

    private String url = "https://us1.locationiq.com/v1";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
