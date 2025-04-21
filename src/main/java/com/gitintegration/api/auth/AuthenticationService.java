package com.gitintegration.api.auth;

import com.gitintegration.api.config.GitServiceFactory;
import com.gitintegration.api.service.GitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling Git provider authentication
 */
@Service
@Slf4j
public class AuthenticationService {
    
    private final AuthenticationConfiguration authConfig;
    private final GitServiceFactory gitServiceFactory;
    private final Map<String, Boolean> authenticationStatus = new HashMap<>();
    
    public AuthenticationService(AuthenticationConfiguration authConfig, GitServiceFactory gitServiceFactory) {
        this.authConfig = authConfig;
        this.gitServiceFactory = gitServiceFactory;
    }
    
    /**
     * Initialize authentication for all providers with configured tokens
     */
    @PostConstruct
    public void init() {
        for (String provider : gitServiceFactory.getAvailableProviders()) {
            String token = authConfig.getToken(provider);
            if (token != null && !token.isEmpty()) {
                authenticate(provider, token);
            }
        }
    }
    
    /**
     * Authenticate a Git provider with the given token
     * @param provider the provider name
     * @param token the authentication token
     * @return true if authentication was successful
     */
    public boolean authenticate(String provider, String token) {
        try {
            GitService service = gitServiceFactory.getService(provider);
            boolean success = service.authenticate(token);
            
            if (success) {
                authConfig.setToken(provider, token);
                authenticationStatus.put(provider, true);
                log.info("Successfully authenticated with {} provider", provider);
            } else {
                authenticationStatus.put(provider, false);
                log.warn("Failed to authenticate with {} provider", provider);
            }
            
            return success;
        } catch (Exception e) {
            log.error("Error authenticating with {} provider: {}", provider, e.getMessage());
            authenticationStatus.put(provider, false);
            return false;
        }
    }
    
    /**
     * Check if a provider is authenticated
     * @param provider the provider name
     * @return true if the provider is authenticated
     */
    public boolean isAuthenticated(String provider) {
        Boolean status = authenticationStatus.get(provider);
        return status != null && status;
    }
    
    /**
     * Get the token for a provider
     * @param provider the provider name
     * @return the authentication token, or null if not found
     */
    public String getToken(String provider) {
        return authConfig.getToken(provider);
    }
    
    /**
     * Get the authentication status for all providers
     * @return a map of provider -> authentication status
     */
    public Map<String, Boolean> getAuthenticationStatus() {
        return new HashMap<>(authenticationStatus);
    }
}