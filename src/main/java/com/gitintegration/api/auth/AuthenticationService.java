package com.gitintegration.api.auth;

import com.gitintegration.api.exception.GitApiException;
import com.gitintegration.api.service.GitService;
import com.gitintegration.api.config.GitServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("authenticationService")
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final GitServiceFactory gitServiceFactory;
    private final Map<String, String> authTokens = new ConcurrentHashMap<>();

    public boolean authenticate(String token) {
        try {
            GitService gitService = gitServiceFactory.getService("github"); // Default to GitHub
            return gitService.authenticate(token);
        } catch (Exception e) {
            log.error("Authentication failed", e);
            return false;
        }
    }

    public boolean authenticate(String provider, String token) {
        String normalizedProvider = provider.toLowerCase();
        try {
            GitService gitService = gitServiceFactory.getService(normalizedProvider);
            boolean authenticated = gitService.authenticate(token);
            if (authenticated) {
                authTokens.put(normalizedProvider, token);
                log.info("Successfully authenticated with {}", normalizedProvider);
            }
            return authenticated;
        } catch (Exception e) {
            log.error("Authentication failed for provider: {}", normalizedProvider, e);
            return false;
        }
    }

    public boolean isAuthenticated(String provider) {
        return authTokens.containsKey(provider.toLowerCase());
    }

    public Map<String, Boolean> getAuthenticationStatus() {
        Map<String, Boolean> status = new HashMap<>();
        for (String provider : gitServiceFactory.getAvailableProviders()) {
            status.put(provider, isAuthenticated(provider));
        }
        return status;
    }

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