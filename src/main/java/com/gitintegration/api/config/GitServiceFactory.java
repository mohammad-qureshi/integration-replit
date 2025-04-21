package com.gitintegration.api.config;

import com.gitintegration.api.exception.GitApiException;
import com.gitintegration.api.service.GitHubService;
import com.gitintegration.api.service.GitLabService;
import com.gitintegration.api.service.GitService;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Factory for creating GitService implementations based on provider
 * This follows the Factory pattern to create the appropriate service implementation
 */
@Component
public class GitServiceFactory {
    
    private final Map<String, GitService> serviceMap = new HashMap<>();
    private final List<String> availableProviders = new ArrayList<>();
    
    public GitServiceFactory(GitHubService gitHubService, GitLabService gitLabService) {
        // Register services
        serviceMap.put("github", gitHubService);
        serviceMap.put("gitlab", gitLabService);
        
        availableProviders.add("github");
        availableProviders.add("gitlab");
    }
    
    /**
     * Get a GitService implementation for the specified provider
     * @param provider the Git provider (e.g., "github", "gitlab")
     * @return the appropriate GitService implementation
     * @throws GitApiException if the provider is not supported
     */
    public GitService getService(String provider) {
        String normalizedProvider = provider.toLowerCase();
        GitService service = serviceMap.get(normalizedProvider);
        
        if (service == null) {
            throw new GitApiException("Unsupported Git provider: " + provider);
        }
        
        return service;
    }
    
    /**
     * Get all available Git providers
     * @return list of available provider names
     */
    public List<String> getAvailableProviders() {
        return Collections.unmodifiableList(availableProviders);
    }
}