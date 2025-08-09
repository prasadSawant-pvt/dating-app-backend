package com.example.techiedating.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryConfig.class);
    private static final String CLOUDINARY_URL = "CLOUDINARY_URL";
    
    // Fallback properties if not using environment variables
    @Value("${cloudinary.cloud.name:}")
    private String cloudName;

    @Value("${cloudinary.api.key:}")
    private String apiKey;

    @Value("${cloudinary.api.secret:}")
    private String apiSecret;
    
    private String cloudinaryUrl;

    @PostConstruct
    public void init() {
        // Try to load from environment variables first
        cloudinaryUrl = System.getenv(CLOUDINARY_URL);
        
        // If not found in system environment, try to load from .env file
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            try {
                Dotenv dotenv = Dotenv.load();
                cloudinaryUrl = dotenv.get(CLOUDINARY_URL);
            } catch (Exception e) {
                logger.warn("No .env file found or error loading .env file. Using application properties.");
            }
        }
        
        if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
            logger.info("Cloudinary configuration loaded from environment variable");
        } else if ((cloudName != null && !cloudName.isEmpty()) && 
                  (apiKey != null && !apiKey.isEmpty()) && 
                  (apiSecret != null && !apiSecret.isEmpty())) {
            logger.info("Cloudinary configuration loaded from application properties");
        } else {
            logger.warn("No Cloudinary configuration found. Please set CLOUDINARY_URL environment variable or configure cloudinary properties in application.properties");
        }
    }

    @Bean
    public Cloudinary cloudinary() {
        if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
            return new Cloudinary(cloudinaryUrl);
        } else {
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
        }
    }
}
