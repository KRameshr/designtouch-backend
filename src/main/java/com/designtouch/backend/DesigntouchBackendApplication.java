package com.designtouch.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

import com.designtouch.backend.config.diagnostics.StartupDiagnostics;
import com.designtouch.backend.config.diagnostics.StartupDiagnosticsServletWebServerApplicationContext;

@SpringBootApplication
@EnableScheduling
public class DesigntouchBackendApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DesigntouchBackendApplication.class);
        if (StartupDiagnostics.isEnabled()) {
            application.setApplicationContextFactory(ApplicationContextFactory.ofContextClass(
                    StartupDiagnosticsServletWebServerApplicationContext.class));
            BufferingApplicationStartup startup = new BufferingApplicationStartup(
                    StartupDiagnostics.STARTUP_STEP_BUFFER_CAPACITY);
            StartupDiagnostics diagnostics = new StartupDiagnostics(startup);
            application.setApplicationStartup(startup);
            application.addListeners(diagnostics);
            application.addInitializers(context ->
                    context.getBeanFactory().addBeanPostProcessor(diagnostics));
        }
        application.run(args);
    }

}
