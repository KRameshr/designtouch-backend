package com.designtouch.backend.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/** Removes credentials accidentally copied into DATABASE_URL query parameters. */
public class DatabaseUrlSanitizerEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(DatabaseUrlSanitizerEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.acceptsProfiles("prod")) {
            return;
        }

        String url = environment.getProperty("DATABASE_URL");
        if (url == null || !url.startsWith("jdbc:postgresql:")) {
            return;
        }

        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return;
        }

        List<String> retainedParameters = new ArrayList<>();
        boolean removedCredentials = false;
        for (String parameter : url.substring(queryStart + 1).split("&", -1)) {
            if (parameter.isEmpty()) {
                continue;
            }

            String encodedKey = parameter.split("=", 2)[0];
            String decodedKey;
            try {
                decodedKey = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException malformedEncoding) {
                // Preserve malformed or future parameters rather than failing startup.
                retainedParameters.add(parameter);
                continue;
            }

            if (decodedKey.equalsIgnoreCase("user") || decodedKey.equalsIgnoreCase("password")) {
                removedCredentials = true;
            } else {
                retainedParameters.add(parameter);
            }
        }

        if (removedCredentials) {
            String sanitized = url.substring(0, queryStart)
                    + (retainedParameters.isEmpty() ? "" : "?" + String.join("&", retainedParameters));
            Map<String, Object> override = new LinkedHashMap<>();
            override.put("spring.datasource.url", sanitized);
            environment.getPropertySources().addFirst(new MapPropertySource("sanitizedDatabaseUrl", override));
            log.warn("Removed username/password query parameters from DATABASE_URL; datasource credentials will use DATABASE_USERNAME and DATABASE_PASSWORD");
        }
    }
}
