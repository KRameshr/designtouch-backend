package com.designtouch.backend.config;

import com.cloudinary.Cloudinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Cloudinary media storage and video hosting.
 *
 * Credentials can be supplied via environment variables:
 *   - CLOUDINARY_CLOUD_NAME
 *   - CLOUDINARY_API_KEY
 *   - CLOUDINARY_API_SECRET
 *
 * Or via application properties:
 *   - cloudinary.cloud-name
 *   - cloudinary.api-key
 *   - cloudinary.api-secret
 *
 * The bean will instantiate safely even when credentials are empty during
 * initial local setup so the backend can boot cleanly without errors.
 */
@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName != null ? cloudName.trim() : "");
        config.put("api_key", apiKey != null ? apiKey.trim() : "");
        config.put("api_secret", apiSecret != null ? apiSecret.trim() : "");
        config.put("secure", true);

        if (cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank()) {
            log.info("Cloudinary configuration loaded successfully (cloud: {})", cloudName.trim());
        } else {
            log.info("Cloudinary configuration loaded (credentials not configured)");
        }

        return new Cloudinary(config);
    }
}

