package com.designtouch.backend.service;

import com.designtouch.backend.controller.NewsletterSubscriberController;
import com.designtouch.backend.repository.NewsletterSubscriberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class EmailServiceContextTest {

    @Test
    @DisplayName("Verify EmailService, NewsletterService, and NewsletterSubscriberController instantiation by Spring IoC")
    void springInstantiatesNewsletterBeansSuccessfully() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("testProperties", Map.of(
                            "resend.api-key", "re_test_key_12345",
                            "resend.from-email", "DesignTouch+ <onboarding@resend.dev>",
                            "resend.enabled", "true"
                    ))
            );

            // Register ObjectMapper dependency
            Supplier<ObjectMapper> mapperSupplier = ObjectMapper::new;
            context.registerBean(ObjectMapper.class, mapperSupplier);

            // Register repository stub for NewsletterService with proper proxy hashCode/equals
            NewsletterSubscriberRepository mockRepo = (NewsletterSubscriberRepository) Proxy.newProxyInstance(
                    NewsletterSubscriberRepository.class.getClassLoader(),
                    new Class<?>[]{NewsletterSubscriberRepository.class},
                    (proxy, method, args) -> {
                        if ("hashCode".equals(method.getName())) {
                            return 42;
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                        if ("toString".equals(method.getName())) {
                            return "MockNewsletterSubscriberRepository";
                        }
                        return null;
                    }
            );
            context.registerBean(NewsletterSubscriberRepository.class, () -> mockRepo);

            // Register service and controller components
            context.register(NewsletterRateLimiter.class);
            context.register(EmailService.class);
            context.register(NewsletterService.class);
            context.register(NewsletterSubscriberController.class);

            // Initialize Spring container
            context.refresh();

            // Verify all beans are created and wired
            EmailService emailService = context.getBean(EmailService.class);
            assertThat(emailService).isNotNull();

            NewsletterService newsletterService = context.getBean(NewsletterService.class);
            assertThat(newsletterService).isNotNull();

            NewsletterSubscriberController controller = context.getBean(NewsletterSubscriberController.class);
            assertThat(controller).isNotNull();

            // Verify EmailService behavior
            assertThat(emailService.sendWelcomeEmail("test@example.com")).isFalse();
        }
    }
}

