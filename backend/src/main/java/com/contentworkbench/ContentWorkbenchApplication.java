package com.contentworkbench;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the AI Content Workbench backend — bootstraps all auto-configuration,
 * component scanning, and the embedded server.
 */
@SpringBootApplication
public class ContentWorkbenchApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentWorkbenchApplication.class, args);
    }
}
