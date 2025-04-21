package com.gitintegration.api.config;

import com.gitintegration.api.exception.GitApiException;
import com.gitintegration.api.service.GitService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for getting GitService implementations by provider name
 */
@Component
public class GitServiceFactory {
    
    private final Map<String, GitService> serviceMap;
    
    public GitServiceFactory(List<GitService> services) {
        // Create a map of provider name -> service implementation
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(
                        GitService::getProviderName,
                        Function.identity()
                ));
    }
    
    /**
     * Get the appropriate GitService implementation for a provider
     * @param provider the provider name (e.g., "github", "gitlab")
     * @return the GitService implementation
     * @throws GitApiException if the provider is not supported
     */
    public GitService getService(String provider) {
        GitService service = serviceMap.get(provider);
        if (service == null) {
            throw new GitApiException("Unsupported Git provider: " + provider);
        }
        return service;
    }
    
    /**
     * Get all available Git service providers
     * @return a list of provider names
     */
    public List<String> getAvailableProviders() {
        return List.copyOf(serviceMap.keySet());
    }
}