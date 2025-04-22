package com.rishabh.fiveday.integration.binding;

import org.springframework.stereotype.Service;

import com.rishabh.fiveday.integration.config.GitServiceFactory;
import com.rishabh.fiveday.integration.dto.RepositoryDTO;
import com.rishabh.fiveday.integration.exception.GitApiException;
import com.rishabh.fiveday.integration.service.GitService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing repository bindings
 */
@Service
public class RepositoryBindingService {

    private final GitServiceFactory gitServiceFactory;
    
    // In-memory storage for repository bindings (would be replaced with a database in a real app)
    private final Map<String, Map<String, RepositoryBinding>> bindingsByProject = new ConcurrentHashMap<>();
    private final Map<String, RepositoryBinding> bindingsById = new ConcurrentHashMap<>();

    public RepositoryBindingService(GitServiceFactory gitServiceFactory) {
        this.gitServiceFactory = gitServiceFactory;
    }

    /**
     * Create a new repository binding
     * @param projectId the project ID to bind to
     * @param provider the Git provider (e.g., "github", "gitlab")
     * @param repositoryId the repository ID in the provider's format
     * @param name a name for the binding
     * @param description a description for the binding
     * @return the created binding
     */
    public RepositoryBinding createBinding(String projectId, String provider, String repositoryId, 
                                          String name, String description) {
        // Verify that the repository exists
        GitService gitService = gitServiceFactory.getService(provider);
        Optional<RepositoryDTO> repoOpt = gitService.getRepository(repositoryId);
        
        if (repoOpt.isEmpty()) {
            throw new GitApiException("Repository not found: " + repositoryId);
        }
        
        // Create the binding
        RepositoryBinding binding = RepositoryBinding.builder()
                .id(UUID.randomUUID().toString())
                .projectId(projectId)
                .repositoryId(repositoryId)
                .provider(provider)
                .name(name != null ? name : repoOpt.get().getName())
                .description(description != null ? description : repoOpt.get().getDescription())
                .build();
        
        // Store the binding
        bindingsById.put(binding.getId(), binding);
        Map<String, RepositoryBinding> projectBindings = bindingsByProject.computeIfAbsent(
                projectId, k -> new ConcurrentHashMap<>());
        projectBindings.put(binding.getId(), binding);
        
        return binding;
    }

    /**
     * Get all bindings for a project
     * @param projectId the project ID
     * @return a list of repository bindings
     */
    public List<RepositoryBinding> getBindingsByProject(String projectId) {
        Map<String, RepositoryBinding> projectBindings = bindingsByProject.get(projectId);
        if (projectBindings == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(projectBindings.values());
    }

    /**
     * Get a binding by ID
     * @param id the binding ID
     * @return the binding if found
     */
    public Optional<RepositoryBinding> getBindingById(String id) {
        return Optional.ofNullable(bindingsById.get(id));
    }

    /**
     * Update a binding
     * @param id the binding ID
     * @param name the new name (null to keep existing)
     * @param description the new description (null to keep existing)
     * @return the updated binding
     */
    public RepositoryBinding updateBinding(String id, String name, String description) {
        RepositoryBinding binding = bindingsById.get(id);
        if (binding == null) {
            throw new GitApiException("Binding not found: " + id);
        }
        
        if (name != null) {
            binding.setName(name);
        }
        
        if (description != null) {
            binding.setDescription(description);
        }
        
        return binding;
    }

    /**
     * Delete a binding
     * @param id the binding ID
     * @return true if the binding was deleted
     */
    public boolean deleteBinding(String id) {
        RepositoryBinding binding = bindingsById.remove(id);
        if (binding != null) {
            Map<String, RepositoryBinding> projectBindings = bindingsByProject.get(binding.getProjectId());
            if (projectBindings != null) {
                projectBindings.remove(id);
            }
            return true;
        }
        return false;
    }
}