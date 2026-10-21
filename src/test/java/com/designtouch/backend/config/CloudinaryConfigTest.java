package com.designtouch.backend.config;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CloudinaryConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CloudinaryConfig.class);

    @Test
    void testCloudinaryBeanCreatedWithEmptyCredentials() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Cloudinary.class);
            Cloudinary cloudinary = context.getBean(Cloudinary.class);
            assertThat(cloudinary).isNotNull();
            assertThat(cloudinary.config.cloudName).isEqualTo("");
        });
    }

    @Test
    void testCloudinaryBeanCreatedWithProvidedCredentials() {
        contextRunner
                .withPropertyValues(
                        "cloudinary.cloud-name=test-cloud",
                        "cloudinary.api-key=test-key",
                        "cloudinary.api-secret=test-secret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(Cloudinary.class);
                    Cloudinary cloudinary = context.getBean(Cloudinary.class);
                    assertThat(cloudinary).isNotNull();
                    assertThat(cloudinary.config.cloudName).isEqualTo("test-cloud");
                    assertThat(cloudinary.config.apiKey).isEqualTo("test-key");
                    assertThat(cloudinary.config.apiSecret).isEqualTo("test-secret");
                    assertThat(cloudinary.config.secure).isTrue();
                });
    }
}

