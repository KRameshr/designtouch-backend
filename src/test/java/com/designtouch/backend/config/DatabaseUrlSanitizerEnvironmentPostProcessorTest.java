package com.designtouch.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseUrlSanitizerEnvironmentPostProcessorTest {
    private final DatabaseUrlSanitizerEnvironmentPostProcessor processor =
            new DatabaseUrlSanitizerEnvironmentPostProcessor();

    @Test
    void prodRemovesOnlyDecodedCredentialParametersAndAppliesSanitizedDatasourceUrl() {
        String originalUrl = "jdbc:postgresql://host:5432/neondb?sslmode=require&channel_binding=require"
                + "&options=-c%20search_path%3Dpublic&%75ser=alice&pa%73sword=secret"
                + "&application_name=design+touch&future=a%26b&userid=keep&malformed%ZZ=keep";
        String expectedUrl = "jdbc:postgresql://host:5432/neondb?sslmode=require&channel_binding=require"
                + "&options=-c%20search_path%3Dpublic&application_name=design+touch"
                + "&future=a%26b&userid=keep&malformed%ZZ=keep";
        ConfigurableEnvironment environment = environment("prod", originalUrl);

        processor.postProcessEnvironment(environment, null);

        assertEquals(expectedUrl, environment.getProperty("spring.datasource.url"));
        assertEquals(originalUrl, environment.getProperty("DATABASE_URL"));
    }

    @Test
    void nonProdProfileLeavesDatasourceUrlUnchanged() {
        String originalUrl = "jdbc:postgresql://host/neondb?user=alice&password=secret&sslmode=require";
        ConfigurableEnvironment environment = environment("dev", originalUrl);

        processor.postProcessEnvironment(environment, null);

        assertEquals(originalUrl, environment.getProperty("spring.datasource.url"));
    }

    private static ConfigurableEnvironment environment(String profile, String databaseUrl) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles(profile);
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "DATABASE_URL", databaseUrl,
                "spring.datasource.url", "${DATABASE_URL}")));
        return environment;
    }
}
