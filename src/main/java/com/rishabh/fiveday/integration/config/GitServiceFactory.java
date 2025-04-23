package com.rishabh.fiveday.integration.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.rishabh.fiveday.integration.exception.GitApiException;
import com.rishabh.fiveday.integration.service.GitService;
import com.rishabh.fiveday.integration.service.impl.GitHubServiceImpl;
import com.rishabh.fiveday.integration.service.impl.GitLabServiceImpl;

/**
 * Factory for creating GitService implementations based on provider
 * This follows the Factory pattern to create the appropriate service implementation
 */
@Component
public class GitServiceFactory {
    
    private final Map<String, GitService> serviceMap = new HashMap<>();
    private final List<String> availableProviders = new ArrayList<>();
    
    public GitServiceFactory(GitHubServiceImpl gitHubService, GitLabServiceImpl gitLabService) {
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
    
    // getService method based on tenant
    // get tenatId from TenantContext
    // then get the provider from redis cache based on tenantId
    // then call the getService method with the provider
	public GitService getService() {
//		String tenantId = TenantContext.getTenantId();
		String provider = "github";//getProviderFromCache(tenantId);
		return getService(provider);
	}
    
    
    /**
     * Get all available Git providers
     * @return list of available provider names
     */
    public List<String> getAvailableProviders() {
        return Collections.unmodifiableList(availableProviders);
    }
}