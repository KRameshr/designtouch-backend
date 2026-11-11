package com.designtouch.backend.config.diagnostics;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.mock.web.MockServletContext;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StartupDiagnosticsServletWebServerApplicationContextTest {

    @Test
    void timedInitializersPreserveBootOrderAndInvokeEachCallbackOnce() throws Exception {
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        List<String> calls = new ArrayList<>();
        beanFactory.addBean("later", initializer("later", 20, calls, null));
        beanFactory.addBean("earlier", initializer("earlier", 10, calls, null));

        var initializers = new StartupDiagnosticsServletWebServerApplicationContext
                .TimedServletContextInitializerBeans(beanFactory);
        ServletContext servletContext = new MockServletContext();

        assertThatCode(() -> {
            for (ServletContextInitializer initializer : initializers) {
                initializer.onStartup(servletContext);
            }
        }).doesNotThrowAnyException();

        assertThat(calls).containsExactly("earlier", "later");
    }

    @Test
    void timedInitializerPropagatesTheSameExceptionAndStopsFollowingCallbacks() {
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        List<String> calls = new ArrayList<>();
        ServletException expected = new ServletException("initializer failed");
        beanFactory.addBean("failing", initializer("failing", 10, calls, expected));
        beanFactory.addBean("following", initializer("following", 20, calls, null));

        var initializers = new StartupDiagnosticsServletWebServerApplicationContext
                .TimedServletContextInitializerBeans(beanFactory);

        assertThatThrownBy(() -> {
            for (ServletContextInitializer initializer : initializers) {
                initializer.onStartup(new MockServletContext());
            }
        }).isSameAs(expected);
        assertThat(calls).containsExactly("failing");
    }

    private static ServletContextInitializer initializer(String name, int order, List<String> calls,
                                                          ServletException failure) {
        return new TestInitializer(name, order, calls, failure);
    }

    private record TestInitializer(String name, int order, List<String> calls, ServletException failure)
            implements ServletContextInitializer, Ordered {

        @Override
        public void onStartup(ServletContext servletContext) throws ServletException {
            calls.add(name);
            if (failure != null) {
                throw failure;
            }
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}
