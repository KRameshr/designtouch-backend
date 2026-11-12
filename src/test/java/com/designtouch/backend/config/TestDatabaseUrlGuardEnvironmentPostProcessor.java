package com.designtouch.backend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.net.URI;

/** Prevents test-classpath Spring contexts from opening a non-local or shared database connection. */
public final class TestDatabaseUrlGuardEnvironmentPostProcessor
        implements EnvironmentPostProcessor, Ordered {

    private static final String TEST_URL = "TEST_DATABASE_URL";
    private static final String TEST_USERNAME = "TEST_DATABASE_USERNAME";
    private static final String TEST_PASSWORD = "TEST_DATABASE_PASSWORD";
    private static final String EXPECTED_USERNAME = "designtouch_test";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configuredTestUrl = environment.getProperty(TEST_URL);
        String effectiveDatasourceUrl = resolveDatasourceUrlIfAvailable(environment);
        boolean hasEffectiveUrl = isResolvedJdbcUrl(effectiveDatasourceUrl);

        if (configuredTestUrl == null && !hasEffectiveUrl) {
            // Lightweight MVC slices need no datasource; full JPA tests fail closed on unresolved placeholders.
            return;
        }

        if (configuredTestUrl != null) {
            requireDedicatedLocalUrl(configuredTestUrl);
        }
        if (hasEffectiveUrl) {
            requireDedicatedLocalUrl(effectiveDatasourceUrl);
            if (configuredTestUrl != null && !configuredTestUrl.equals(effectiveDatasourceUrl)) {
                throw unsafeDatabaseConfiguration();
            }
        }

        String username = environment.getProperty(TEST_USERNAME);
        String password = environment.getProperty(TEST_PASSWORD);
        if (!EXPECTED_USERNAME.equals(username) || password == null || password.isBlank()) {
            throw unsafeDatabaseConfiguration();
        }
    }

    private static boolean isResolvedJdbcUrl(String value) {
        return value != null && value.startsWith("jdbc:") && !value.contains("${");
    }

    /**
     * Resolve the configured property leniently. Datasource-free test slices may
     * load a profile property such as ${TEST_DATABASE_URL} without defining that
     * variable; strict Environment.getProperty resolution throws before the
     * guard can recognize that no datasource is configured for the slice.
     */
    private static String resolveDatasourceUrlIfAvailable(ConfigurableEnvironment environment) {
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            Object value = propertySource.getProperty("spring.datasource.url");
            if (value instanceof String rawValue) {
                return environment.resolvePlaceholders(rawValue);
            }
        }
        return null;
    }

    private static void requireDedicatedLocalUrl(String value) {
        try {
            URI uri = URI.create(value.substring("jdbc:".length()));
            if (!"postgresql".equals(uri.getScheme())
                    || !"127.0.0.1".equals(uri.getHost())
                    || uri.getPort() != 5432
                    || !"/designtouch_test".equals(uri.getPath())
                    || uri.getUserInfo() != null
                    || uri.getQuery() != null
                    || uri.getFragment() != null) {
                throw unsafeDatabaseConfiguration();
            }
        } catch (IllegalArgumentException ex) {
            throw unsafeDatabaseConfiguration();
        }
    }

    private static IllegalStateException unsafeDatabaseConfiguration() {
        return new IllegalStateException(
                "Tests require the dedicated local PostgreSQL database designtouch_test at 127.0.0.1:5432 "
                        + "with role designtouch_test. Remote, shared, and production databases are refused.");
    }
}
