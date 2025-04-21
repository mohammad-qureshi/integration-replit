package com.gitintegration.api.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Git provider authentication
 */
@Configuration
@ConfigurationProperties(prefix = "git.auth")
public class AuthenticationConfiguration {
    
    /**
     * Map of provider -> tokens
     * Example in properties:
     * git.auth.tokens.github=GITHUB_TOKEN
     * git.auth.tokens.gitlab=GITLAB_TOKEN
     */
    private Map<String, String> tokens = new HashMap<>();
    
    public Map<String, String> getTokens() {
        return tokens;
    }
    
    public void setTokens(Map<String, String> tokens) {
        this.tokens = tokens;
    }
    
    public String getToken(String provider) {
        return tokens.get(provider);
    }
    
    public void setToken(String provider, String token) {
        tokens.put(provider, token);
    }
}