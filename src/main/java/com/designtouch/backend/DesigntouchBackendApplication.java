package com.designtouch.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DesigntouchBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DesigntouchBackendApplication.class, args);
    }

}
