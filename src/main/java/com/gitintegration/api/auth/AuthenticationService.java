package com.gitintegration.api.auth;

import com.gitintegration.api.exception.GitApiException;
import com.gitintegration.api.service.GitService;
import com.gitintegration.api.config.GitServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing authentication with Git providers
 */
@Service
@Slf4j
public class AuthenticationService {
    
    private final GitServiceFactory gitServiceFactory;
    
    // For in-memory token storage (would use a secure vault in production)
    private final Map<String, String> authTokens = new ConcurrentHashMap<>();
    
    @Value("${git.auth.tokens.github:}")
    private String githubToken;
    
    @Value("${git.auth.tokens.gitlab:}")
    private String gitlabToken;
    
    public AuthenticationService(GitServiceFactory gitServiceFactory) {
        this.gitServiceFactory = gitServiceFactory;
    }
    
    /**
     * Initialize authentication from environment variables or properties
     * Called after dependencies are injected
     */
    public void init() {
        // Try to authenticate with tokens provided by properties/env vars
        if (githubToken != null && !githubToken.isEmpty()) {
            authenticate("github", githubToken);
        }
        
        if (gitlabToken != null && !gitlabToken.isEmpty()) {
            authenticate("gitlab", gitlabToken);
        }
    }
    
    /**
     * Authenticate with a Git provider
     * @param provider the Git provider (e.g., "github", "gitlab")
     * @param token the authentication token
     * @return true if authentication was successful
     */
    public boolean authenticate(String provider, String token) {
        if (provider == null || token == null || token.isEmpty()) {
            return false;
        }
        
        String normalizedProvider = provider.toLowerCase();
        try {
            GitService service = gitServiceFactory.getService(normalizedProvider);
            service.setAuthToken(token);
            
            // Verify token by making a test request
            if (service.isAuthenticated()) {
                authTokens.put(normalizedProvider, token);
                log.info("Successfully authenticated with {}", normalizedProvider);
                return true;
            } else {
                log.warn("Failed to authenticate with {}", normalizedProvider);
                return false;
            }
        } catch (GitApiException e) {
            log.error("Error authenticating with {}: {}", normalizedProvider, e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if authenticated with a Git provider
     * @param provider the Git provider
     * @return true if authenticated
     */
    public boolean isAuthenticated(String provider) {
        String normalizedProvider = provider.toLowerCase();
        return authTokens.containsKey(normalizedProvider);
    }
    
    /**
     * Get the authentication token for a provider
     * @param provider the Git provider
     * @return the token if authenticated, or null
     */
    public String getToken(String provider) {
        String normalizedProvider = provider.toLowerCase();
        return authTokens.get(normalizedProvider);
    }
    
    /**
     * Get authentication status for all providers
     * @return map of provider names to authentication status
     */
    public Map<String, Boolean> getAuthenticationStatus() {
        Map<String, Boolean> status = new HashMap<>();
        for (String provider : gitServiceFactory.getAvailableProviders()) {
            status.put(provider, isAuthenticated(provider));
        }
        return status;
    }
    
    /**
     * Revoke authentication for a provider
     * @param provider the Git provider
     * @return true if revoked, false if not authenticated
     */
    public boolean revokeAuthentication(String provider) {
        String normalizedProvider = provider.toLowerCase();
        String token = authTokens.remove(normalizedProvider);
        if (token != null) {
            log.info("Revoked authentication for {}", normalizedProvider);
            return true;
        }
        return false;
    }
}