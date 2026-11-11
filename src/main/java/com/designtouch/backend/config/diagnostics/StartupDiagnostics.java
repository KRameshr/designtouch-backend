package com.designtouch.backend.config.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.IMetricsTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.metrics.buffering.StartupTimeline;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Opt-in startup timing. Emits only lifecycle and bean identifiers, never configuration values. */
public final class StartupDiagnostics implements ApplicationListener<ApplicationEvent>, BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StartupDiagnostics.class);
    private static final String ENABLED_ENV = "APP_STARTUP_DIAGNOSTICS_ENABLED";
    private static final int MAX_STEP_LOGS = 25;
    private static final int MAX_BEAN_LOGS = 15;
    public static final int STARTUP_STEP_BUFFER_CAPACITY = 2048;

    private final BufferingApplicationStartup startup;
    private final long processStartMillis = ManagementFactory.getRuntimeMXBean().getStartTime();
    private final AtomicLong processToSpringStartingMillis = new AtomicLong(-1);
    private final AtomicLong processToApplicationReadyMillis = new AtomicLong(-1);
    private final Map<String, Long> milestones = new ConcurrentHashMap<>();
    private final Map<String, BeanTimer> beanTimers = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<TimedBean> measurements = new ConcurrentLinkedQueue<>();

    public StartupDiagnostics(BufferingApplicationStartup startup) {
        this.startup = startup;
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(System.getenv(ENABLED_ENV));
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            mark("springStarting");
            processToSpringStartingMillis.set(elapsedWallClockMillis(processStartMillis, System.currentTimeMillis()));
        } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
            mark("environmentPrepared");
        } else if (event instanceof ApplicationContextInitializedEvent) {
            mark("contextInitialized");
        } else if (event instanceof ApplicationPreparedEvent) {
            mark("applicationPrepared");
        } else if (event instanceof ContextRefreshedEvent refreshed && refreshed.getApplicationContext().getParent() == null) {
            mark("contextRefreshed");
        } else if (event instanceof WebServerInitializedEvent webServerInitialized) {
            mark("webServerInitialized");
            String serverType = webServerInitialized.getWebServer().getClass().getSimpleName();
            String stage = serverType.contains("Tomcat") ? "tomcat.web-server-initialized" : "webserver.initialized";
            Long prepared = milestones.get("applicationPrepared");
            log.info("Startup diagnostic stage={} status=initialized serverType={} preparedToInitializedMs={}",
                    stage, serverType, prepared == null ? -1 : millis(milestoneElapsed(prepared)));
        } else if (event instanceof ApplicationStartedEvent) {
            mark("applicationStarted");
        } else if (event instanceof ApplicationReadyEvent ready) {
            mark("applicationReady");
            processToApplicationReadyMillis.set(elapsedWallClockMillis(processStartMillis, System.currentTimeMillis()));
            logSummary(ready);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        long now = System.nanoTime();
        beanTimers.put(beanName, new BeanTimer(now, bean.getClass().getName()));
        if (bean instanceof FlywayMigrationInitializer) {
            beanTimers.put("@stage:flyway", new BeanTimer(now, beanName));
        } else if (bean instanceof LocalContainerEntityManagerFactoryBean) {
            beanTimers.put("@stage:entityManagerFactory", new BeanTimer(now, beanName));
        } else if (bean instanceof RepositoryFactoryBeanSupport<?, ?, ?>) {
            beanTimers.put("@repository:" + beanName, new BeanTimer(now, bean.getClass().getName()));
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        long now = System.nanoTime();
        BeanTimer timer = beanTimers.remove(beanName);
        if (timer != null) {
            measurements.add(new TimedBean("bean.initialization", beanName, timer.beanType(), now - timer.startedAtNanos()));
        }

        if (bean instanceof FlywayMigrationInitializer) {
            recordSpecial("flyway.validation-and-migration", "flywayMigrationInitializer");
        } else if (bean instanceof LocalContainerEntityManagerFactoryBean) {
            recordSpecial("hibernate.entityManagerFactory", beanName);
        } else if (bean instanceof RepositoryFactoryBeanSupport<?, ?, ?>) {
            BeanTimer repositoryTimer = beanTimers.remove("@repository:" + beanName);
            if (repositoryTimer != null) {
                measurements.add(new TimedBean("jpa.repository.initialization", beanName,
                        repositoryTimer.beanType(), now - repositoryTimer.startedAtNanos()));
            }
        }

        if (bean instanceof HikariDataSource hikariDataSource) {
            installHikariMetrics(hikariDataSource);
        }
        return bean;
    }

    private void installHikariMetrics(HikariDataSource dataSource) {
        if (dataSource.getMetricsTrackerFactory() != null) {
            log.info("Startup diagnostic stage=hikari.metrics status=existing-tracker-preserved");
            return;
        }
        try {
            dataSource.setMetricsTrackerFactory((poolName, poolStats) -> new FirstConnectionMetricsTracker(this));
        } catch (IllegalStateException ex) {
            // Diagnostics must never prevent the application from starting.
            log.warn("Startup diagnostic stage=hikari.metrics status=not-installed exceptionType={}",
                    ex.getClass().getSimpleName());
        }
    }

    private void recordSpecial(String stage, String key) {
        BeanTimer timer = beanTimers.remove("@stage:" + (stage.startsWith("flyway") ? "flyway" : "entityManagerFactory"));
        if (timer != null) {
            TimedBean measurement = new TimedBean(stage, key, timer.beanType(), System.nanoTime() - timer.startedAtNanos());
            measurements.add(measurement);
            log.info("Startup diagnostic stage={} durationMs={} bean={}", stage, millis(measurement.durationNanos()), key);
        }
    }

    private void recordHikariTiming(String stage, long durationNanos) {
        TimedBean measurement = new TimedBean(stage, "dataSource", HikariDataSource.class.getName(), durationNanos);
        measurements.add(measurement);
        log.info("Startup diagnostic stage={} durationMs={}", measurement.stage(), millis(durationNanos));
    }

    private void logSummary(ApplicationReadyEvent ready) {
        try {
            long processToSpringStart = processToSpringStartingMillis.get();
            if (processToSpringStart >= 0) {
                log.info("Startup diagnostic stage=jvm.process-to-spring-starting durationMs={}", processToSpringStart);
            } else {
                log.info("Startup diagnostic stage=jvm.process-to-spring-starting status=not-measured");
            }
            logMilestone("spring.starting-to-environment", "springStarting", "environmentPrepared");
            logMilestone("spring.environment-to-context-created", "environmentPrepared", "contextInitialized");
            logMilestone("spring.context-definition-loading", "contextInitialized", "applicationPrepared");
            logMilestone("spring.context-refresh", "applicationPrepared", "contextRefreshed");
            logMilestone("spring.context-initialization", "contextInitialized", "contextRefreshed");
            logMilestone("spring.post-refresh-to-started", "contextRefreshed", "applicationStarted");
            logMilestone("spring.started-to-ready", "applicationStarted", "applicationReady");
            long processToReady = processToApplicationReadyMillis.get();
            log.info("Startup diagnostic stage=process-to-ApplicationReadyEvent durationMs={}", processToReady);
            log.info("Startup diagnostic stage=ApplicationReadyEvent beanDefinitionCount={} eventTimeTakenMs={}",
                    ready.getApplicationContext().getBeanFactory().getBeanDefinitionCount(),
                    ready.getTimeTaken() == null ? -1 : ready.getTimeTaken().toMillis());
            logBeanMeasurements();
            logStartupSteps();
            logCoverage();
        } catch (RuntimeException ex) {
            log.warn("Startup diagnostics summary failed; exceptionType={}", ex.getClass().getSimpleName());
        }
    }

    private void logMilestone(String label, String start, String end) {
        Long started = milestones.get(start);
        Long finished = milestones.get(end);
        if (started != null && finished != null && finished >= started) {
            log.info("Startup diagnostic stage={} durationMs={}", label, millis(finished - started));
        } else {
            log.info("Startup diagnostic stage={} status=not-measured", label);
        }
    }

    private void logBeanMeasurements() {
        List<TimedBean> all = new ArrayList<>(measurements);
        List<TimedBean> beanInitializations = all.stream()
                .filter(m -> m.stage().equals("bean.initialization")).toList();
        long beanInitializationNanos = beanInitializations.stream().mapToLong(TimedBean::durationNanos).sum();
        log.info("Startup diagnostic stage=bean.initialization.total durationMs={} beanCount={}",
                millis(beanInitializationNanos), beanInitializations.size());
        for (String stage : List.of("jpa.repository.initialization", "flyway.validation-and-migration",
                "hibernate.entityManagerFactory")) {
            List<TimedBean> stageMeasurements = all.stream().filter(m -> m.stage().equals(stage)).toList();
            if (!stageMeasurements.isEmpty()) {
                long totalNanos = stageMeasurements.stream().mapToLong(TimedBean::durationNanos).sum();
                log.info("Startup diagnostic stage={}.total durationMs={} count={}",
                        stage, millis(totalNanos), stageMeasurements.size());
            }
        }
        all.stream().filter(m -> !m.stage().equals("bean.initialization"))
                .sorted(Comparator.comparingLong(TimedBean::durationNanos).reversed())
                .limit(MAX_BEAN_LOGS)
                .forEach(m -> log.info("Startup diagnostic stage={} durationMs={} bean={} type={}",
                        m.stage(), millis(m.durationNanos()), m.beanName(), m.beanType()));
        all.stream().filter(m -> m.stage().equals("bean.initialization"))
                .sorted(Comparator.comparingLong(TimedBean::durationNanos).reversed())
                .limit(MAX_BEAN_LOGS)
                .forEach(m -> log.info("Startup diagnostic stage={} durationMs={} bean={} type={}",
                        m.stage(), millis(m.durationNanos()), m.beanName(), m.beanType()));
    }

    private void logStartupSteps() {
        StartupTimeline timeline = startup.getBufferedTimeline();
        List<StartupStepMeasurement> relevant = relevantStartupSteps();
        boolean atCapacity = timeline.getEvents().size() >= STARTUP_STEP_BUFFER_CAPACITY;
        log.info("Startup diagnostic stage=spring.startup-steps bufferedStepCount={} loggedStepCount={} bufferCapacity={} truncationPossible={} droppedCount=unknown",
                timeline.getEvents().size(), relevant.size(), STARTUP_STEP_BUFFER_CAPACITY, atCapacity);
        for (StartupStepMeasurement step : relevant) {
            log.info("Startup diagnostic step={} durationMs={} beanInfo={}",
                    step.name(), step.durationMillis(), step.beanInfo());
        }
        if (atCapacity) {
            log.warn("Startup diagnostic startup-step buffer reached capacity; retained events may be incomplete and dropped count is unavailable");
        }
    }

    List<StartupStepMeasurement> relevantStartupSteps() {
        return startup.getBufferedTimeline().getEvents().stream()
                .filter(event -> {
                    String name = event.getStartupStep().getName().toLowerCase();
                    return name.contains("bean") || name.contains("repository") || name.contains("jpa")
                            || name.contains("hibernate") || name.contains("flyway") || name.contains("context")
                            || name.contains("webserver") || name.contains("web.server") || name.contains("tomcat");
                })
                .sorted(Comparator.comparing(StartupTimeline.TimelineEvent::getDuration).reversed())
                .limit(MAX_STEP_LOGS)
                .map(event -> {
                    List<String> tags = new ArrayList<>();
                    event.getStartupStep().getTags().forEach(tag -> {
                        if (tag.getKey().equals("beanName") || tag.getKey().equals("beanType")
                                || tag.getKey().equals("className")) {
                            tags.add(tag.getKey() + "=" + tag.getValue());
                        }
                    });
                    return new StartupStepMeasurement(event.getStartupStep().getName(),
                            event.getDuration().toMillis(), tags.isEmpty() ? "none" : String.join(",", tags));
                }).toList();
    }

    List<TimedBean> measurementsSnapshot() {
        return List.copyOf(measurements);
    }

    private void logCoverage() {
        log.info("Startup diagnostic stage=bean.instrumentation coverage=partial starts=BeanPostProcessor note=beans-created-before-processor-are-not-timed");
        for (String stage : List.of("hikari.first-physical-connection", "hikari.first-connection-acquisition",
                "flyway.validation-and-migration", "hibernate.entityManagerFactory", "jpa.repository.initialization")) {
            boolean measured = measurements.stream().anyMatch(measurement -> measurement.stage().equals(stage));
            log.info("Startup diagnostic stage={} status={}", stage, measured ? "measured" : "not-measured");
        }
        log.info("Startup diagnostic stage=tomcat.web-server-initialized status={}",
                milestones.containsKey("webServerInitialized") ? "measured" : "not-measured");
    }

    private long milestoneElapsed(long startedAtNanos) {
        return Math.max(0, System.nanoTime() - startedAtNanos);
    }

    private void mark(String key) {
        milestones.putIfAbsent(key, System.nanoTime());
    }

    private static long millis(long nanos) {
        return Math.max(0, nanos / 1_000_000L);
    }

    static long elapsedWallClockMillis(long startedAtMillis, long endedAtMillis) {
        return Math.max(0, endedAtMillis - startedAtMillis);
    }

    private record BeanTimer(long startedAtNanos, String beanType) {}
    record TimedBean(String stage, String beanName, String beanType, long durationNanos) {}
    record StartupStepMeasurement(String name, long durationMillis, String beanInfo) {}

    private static final class FirstConnectionMetricsTracker implements IMetricsTracker {
        private final StartupDiagnostics diagnostics;
        private final AtomicBoolean physicalConnectionRecorded = new AtomicBoolean();
        private final AtomicBoolean connectionAcquisitionRecorded = new AtomicBoolean();

        private FirstConnectionMetricsTracker(StartupDiagnostics diagnostics) {
            this.diagnostics = diagnostics;
        }

        @Override
        public void recordConnectionCreatedMillis(long connectionCreationMillis) {
            if (physicalConnectionRecorded.compareAndSet(false, true)) {
                diagnostics.recordHikariTiming("hikari.first-physical-connection",
                        connectionCreationMillis * 1_000_000L);
            }
        }

        @Override
        public void recordConnectionAcquiredNanos(long connectionAcquisitionNanos) {
            if (connectionAcquisitionRecorded.compareAndSet(false, true)) {
                diagnostics.recordHikariTiming("hikari.first-connection-acquisition", connectionAcquisitionNanos);
            }
        }
    }
}
