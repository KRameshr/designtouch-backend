package com.designtouch.backend.config.diagnostics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StartupDiagnosticsTest {

    @Test
    void capturesRelevantSpringStartupStepsAndBeanTags() {
        BufferingApplicationStartup startup = new BufferingApplicationStartup(32);
        StartupDiagnostics diagnostics = new StartupDiagnostics(startup);

        startup.start("spring.beans.instantiate")
                .tag("beanName", "sampleRepository")
                .end();

        assertThat(diagnostics.relevantStartupSteps())
                .anySatisfy(step -> {
                    assertThat(step.name()).isEqualTo("spring.beans.instantiate");
                    assertThat(step.durationMillis()).isGreaterThanOrEqualTo(0);
                    assertThat(step.beanInfo()).contains("beanName=sampleRepository");
                });
    }

    @Test
    void capturesWebServerStartupSteps() {
        BufferingApplicationStartup startup = new BufferingApplicationStartup(32);
        StartupDiagnostics diagnostics = new StartupDiagnostics(startup);

        startup.start("spring.boot.webserver.start").end();

        assertThat(diagnostics.relevantStartupSteps())
                .anySatisfy(step -> assertThat(step.name()).isEqualTo("spring.boot.webserver.start"));
    }

    @Test
    void measuresBeanInitializationWithoutStartingSpringOrConnectingToDatabase() {
        StartupDiagnostics diagnostics = new StartupDiagnostics(new BufferingApplicationStartup(32));
        Object bean = new Object();

        diagnostics.postProcessBeforeInitialization(bean, "sampleBean");
        diagnostics.postProcessAfterInitialization(bean, "sampleBean");

        assertThat(diagnostics.measurementsSnapshot())
                .anySatisfy(measurement -> {
                    assertThat(measurement.stage()).isEqualTo("bean.initialization");
                    assertThat(measurement.beanName()).isEqualTo("sampleBean");
                    assertThat(measurement.durationNanos()).isGreaterThanOrEqualTo(0);
                });
    }

    @Test
    void hikariMetricsPreserveDataSourceIdentityAndRecordConnectionTimings() {
        StartupDiagnostics diagnostics = new StartupDiagnostics(new BufferingApplicationStartup(32));
        HikariDataSource dataSource = new HikariDataSource();

        Object processed = diagnostics.postProcessAfterInitialization(dataSource, "dataSource");

        assertThat(processed).isSameAs(dataSource);
        MetricsTrackerFactory trackerFactory = dataSource.getMetricsTrackerFactory();
        assertThat(trackerFactory).isNotNull();
        IMetricsTracker tracker = trackerFactory.create("diagnostic-test-pool", mock(PoolStats.class));
        tracker.recordConnectionCreatedMillis(12);
        tracker.recordConnectionAcquiredNanos(34);

        assertThat(diagnostics.measurementsSnapshot())
                .extracting(StartupDiagnostics.TimedBean::stage)
                .contains("hikari.first-physical-connection", "hikari.first-connection-acquisition");
        assertThatCode(dataSource::close).doesNotThrowAnyException();
    }

    @Test
    void preservesAnExistingHikariMetricsTracker() {
        StartupDiagnostics diagnostics = new StartupDiagnostics(new BufferingApplicationStartup(32));
        HikariDataSource dataSource = new HikariDataSource();
        MetricsTrackerFactory existingTracker = (poolName, poolStats) -> mock(IMetricsTracker.class);
        dataSource.setMetricsTrackerFactory(existingTracker);

        Object processed = diagnostics.postProcessAfterInitialization(dataSource, "dataSource");

        assertThat(processed).isSameAs(dataSource);
        assertThat(dataSource.getMetricsTrackerFactory()).isSameAs(existingTracker);
        assertThatCode(dataSource::close).doesNotThrowAnyException();
    }

    @Test
    void flywayStageCoversTheInitializerMigrationCall() throws Exception {
        StartupDiagnostics diagnostics = new StartupDiagnostics(new BufferingApplicationStartup(32));
        Flyway flyway = mock(Flyway.class);
        FlywayMigrationInitializer initializer = new FlywayMigrationInitializer(flyway);

        diagnostics.postProcessBeforeInitialization(initializer, "flywayMigrationInitializer");
        initializer.afterPropertiesSet();
        diagnostics.postProcessAfterInitialization(initializer, "flywayMigrationInitializer");

        verify(flyway).migrate();
        assertThat(diagnostics.measurementsSnapshot())
                .anySatisfy(measurement -> assertThat(measurement.stage())
                        .isEqualTo("flyway.validation-and-migration"));
    }

    @Test
    void processElapsedTimeUsesTheEventTimestampAndClampsClockRollback() {
        assertThat(StartupDiagnostics.elapsedWallClockMillis(1_000, 1_250)).isEqualTo(250);
        assertThat(StartupDiagnostics.elapsedWallClockMillis(1_250, 1_000)).isZero();
    }
}
