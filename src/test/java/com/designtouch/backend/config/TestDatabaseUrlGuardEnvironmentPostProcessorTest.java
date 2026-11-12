package com.designtouch.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDatabaseUrlGuardEnvironmentPostProcessorTest {

    private final TestDatabaseUrlGuardEnvironmentPostProcessor processor =
            new TestDatabaseUrlGuardEnvironmentPostProcessor();

    @Test
    void allowsOnlyDedicatedLocalTestDatabaseConfiguration() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("TEST_DATABASE_URL", "jdbc:postgresql://127.0.0.1:5432/designtouch_test")
                .withProperty("TEST_DATABASE_USERNAME", "designtouch_test")
                .withProperty("TEST_DATABASE_PASSWORD", "local-only-test-password")
                .withProperty("spring.datasource.url", "jdbc:postgresql://127.0.0.1:5432/designtouch_test");

        assertThatCode(() -> processor.postProcessEnvironment(environment, new SpringApplication()))
                .doesNotThrowAnyException();
    }

    @Test
    void refusesRemoteDatasourceWithoutEchoingTheUrl() {
        String remoteUrl = "jdbc:postgresql://production.example.invalid/prod";
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", remoteUrl);

        assertThatThrownBy(() -> processor.postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Remote, shared, and production databases are refused")
                .hasMessageNotContaining(remoteUrl);
    }

    @Test
    void refusesAnotherLocalDatabaseName() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("TEST_DATABASE_URL", "jdbc:postgresql://127.0.0.1:5432/neondb")
                .withProperty("TEST_DATABASE_USERNAME", "designtouch_test")
                .withProperty("TEST_DATABASE_PASSWORD", "local-only-test-password");

        assertThatThrownBy(() -> processor.postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining("neondb");
    }

    @Test
    void refusesIncorrectLocalDatabasePort() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("TEST_DATABASE_URL", "jdbc:postgresql://127.0.0.1:5433/designtouch_test")
                .withProperty("TEST_DATABASE_USERNAME", "designtouch_test")
                .withProperty("TEST_DATABASE_PASSWORD", "local-only-test-password");

        assertThatThrownBy(() -> processor.postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Remote, shared, and production databases are refused");
    }

    @Test
    void refusesDedicatedDatabaseWhenTestCredentialsAreMissing() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("TEST_DATABASE_URL", "jdbc:postgresql://127.0.0.1:5432/designtouch_test");

        assertThatThrownBy(() -> processor.postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Remote, shared, and production databases are refused");
    }

    @Test
    void allowsUnresolvedTestDatabasePlaceholderForDatasourceFreeMvcSlices() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "${TEST_DATABASE_URL}");

        assertThatCode(() -> processor.postProcessEnvironment(environment, new SpringApplication()))
                .doesNotThrowAnyException();
    }

    @Test
    void leavesDatasourceFreeMvcSlicesUnaffectedWhenNoTestDatabaseIsConfigured() {
        MockEnvironment environment = new MockEnvironment();

        assertThatCode(() -> processor.postProcessEnvironment(environment, new SpringApplication()))
                .doesNotThrowAnyException();
    }
}
