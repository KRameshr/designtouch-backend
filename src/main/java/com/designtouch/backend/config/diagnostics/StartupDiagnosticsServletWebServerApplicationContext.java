package com.designtouch.backend.config.diagnostics;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Servlet context that times Boot's ordered initializer callbacks when diagnostics are enabled. */
public class StartupDiagnosticsServletWebServerApplicationContext
        extends AnnotationConfigServletWebServerApplicationContext {

    private static final Logger log = LoggerFactory.getLogger(StartupDiagnosticsServletWebServerApplicationContext.class);

    @Override
    protected Collection<ServletContextInitializer> getServletContextInitializerBeans() {
        TimedServletContextInitializerBeans initializers = new TimedServletContextInitializerBeans(getBeanFactory());
        safeInfo("Startup diagnostic stage=tomcat.initializers.begin timestampEpochMs={} callbackCount={}",
                System.currentTimeMillis(), initializers.size());
        return initializers;
    }

    static final class TimedServletContextInitializerBeans extends ServletContextInitializerBeans {

        TimedServletContextInitializerBeans(ListableBeanFactory beanFactory) {
            super(beanFactory);
        }

        @Override
        public Iterator<ServletContextInitializer> iterator() {
            Iterator<ServletContextInitializer> ordered = super.iterator();
            return new Iterator<>() {
                private int callbackIndex;

                @Override
                public boolean hasNext() {
                    return ordered.hasNext();
                }

                @Override
                public ServletContextInitializer next() {
                    if (!ordered.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    int index = ++callbackIndex;
                    ServletContextInitializer initializer = ordered.next();
                    String initializerName = initializer.getClass().getSimpleName();
                    if (initializerName.isEmpty()) {
                        initializerName = initializer.getClass().getName();
                    }
                    String name = initializerName;
                    return servletContext -> invokeTimed(initializer, servletContext, index, name);
                }
            };
        }

        private static void invokeTimed(ServletContextInitializer initializer, ServletContext servletContext,
                                        int callbackIndex, String initializerName) throws ServletException {
            long startTimestampEpochMs = System.currentTimeMillis();
            long startNanos = System.nanoTime();
            boolean succeeded = false;
            try {
                initializer.onStartup(servletContext);
                succeeded = true;
            } finally {
                long endTimestampEpochMs = System.currentTimeMillis();
                long durationMs = Math.max(0, (System.nanoTime() - startNanos) / 1_000_000L);
                safeInfo("Startup diagnostic stage=tomcat.servlet-initializer callbackIndex={} initializerName={} startTimestampEpochMs={} endTimestampEpochMs={} durationMs={} status={}",
                        callbackIndex, initializerName, startTimestampEpochMs, endTimestampEpochMs,
                        durationMs, succeeded ? "success" : "failure");
            }
        }
    }

    private static void safeInfo(String message, Object... arguments) {
        try {
            log.info(message, arguments);
        } catch (RuntimeException ignored) {
            // Diagnostic logging must not change container startup behavior.
        }
    }
}
