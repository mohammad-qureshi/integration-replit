package com.gitintegration.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Git Integration API application.
 * This application integrates with GitHub and GitLab APIs to fetch repository data.
 */
@SpringBootApplication
public class GitIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitIntegrationApplication.class, args);
    }
}
