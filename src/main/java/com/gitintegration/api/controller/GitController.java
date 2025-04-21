package com.gitintegration.api.controller;

import com.gitintegration.api.auth.AuthenticationRequestDTO;
import com.gitintegration.api.auth.AuthenticationResponseDTO;
import com.gitintegration.api.auth.AuthenticationService;
import com.gitintegration.api.binding.RepositoryBinding;
import com.gitintegration.api.binding.RepositoryBindingService;
import com.gitintegration.api.config.GitServiceFactory;
import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;
import com.gitintegration.api.dto.RepositoryDTO;
import com.gitintegration.api.exception.GitApiException;
import com.gitintegration.api.service.GitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unified controller for Git operations
 * This follows the Facade pattern, providing a unified interface to the different Git provider implementations
 */
@RestController
@RequestMapping("/api/git")
@Slf4j
public class GitController {

    private final GitServiceFactory gitServiceFactory;
    private final AuthenticationService authService;
    private final RepositoryBindingService bindingService;

    public GitController(GitServiceFactory gitServiceFactory, AuthenticationService authService, 
                        RepositoryBindingService bindingService) {
        this.gitServiceFactory = gitServiceFactory;
        this.authService = authService;
        this.bindingService = bindingService;
    }

    /**
     * Get available Git providers
     */
    @GetMapping("/providers")
    public ResponseEntity<List<String>> getProviders() {
        return ResponseEntity.ok(gitServiceFactory.getAvailableProviders());
    }

    /**
     * Get authentication status for all providers
     */
    @GetMapping("/auth/status")
    public ResponseEntity<Map<String, Boolean>> getAuthStatus() {
        return ResponseEntity.ok(authService.getAuthenticationStatus());
    }

    /**
     * Authenticate with a Git provider
     */
    @PostMapping("/auth")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@RequestBody AuthenticationRequestDTO request) {
        boolean success = authService.authenticate(request.getProvider(), request.getToken());
        
        AuthenticationResponseDTO response = AuthenticationResponseDTO.builder()
                .provider(request.getProvider())
                .authenticated(success)
                .message(success ? "Authentication successful" : "Authentication failed")
                .build();
        
        return ResponseEntity.ok(response);
    }

    // ======== Repository binding endpoints ========

    /**
     * Create a repository binding
     */
    @PostMapping("/bindings")
    public ResponseEntity<RepositoryBinding> createBinding(
            @RequestParam String projectId,
            @RequestParam String provider,
            @RequestParam String repositoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        RepositoryBinding binding = bindingService.createBinding(
                projectId, provider, repositoryId, name, description);
        
        return new ResponseEntity<>(binding, HttpStatus.CREATED);
    }

    /**
     * Get bindings for a project
     */
    @GetMapping("/bindings")
    public ResponseEntity<List<RepositoryBinding>> getBindings(@RequestParam String projectId) {
        return ResponseEntity.ok(bindingService.getBindingsByProject(projectId));
    }

    /**
     * Get a binding by ID
     */
    @GetMapping("/bindings/{id}")
    public ResponseEntity<RepositoryBinding> getBinding(@PathVariable String id) {
        return bindingService.getBindingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a binding
     */
    @PutMapping("/bindings/{id}")
    public ResponseEntity<RepositoryBinding> updateBinding(
            @PathVariable String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {
        
        RepositoryBinding binding = bindingService.updateBinding(id, name, description);
        return ResponseEntity.ok(binding);
    }

    /**
     * Delete a binding
     */
    @DeleteMapping("/bindings/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteBinding(@PathVariable String id) {
        boolean deleted = bindingService.deleteBinding(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        return ResponseEntity.ok(response);
    }

    // ======== Repository endpoints ========

    /**
     * Get repositories for a provider
     */
    @GetMapping("/repositories")
    public ResponseEntity<List<RepositoryDTO>> getRepositories(@RequestParam String provider) {
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        return ResponseEntity.ok(gitService.getRepositories());
    }

    /**
     * Get a repository
     */
    @GetMapping("/repositories/{repositoryId}")
    public ResponseEntity<RepositoryDTO> getRepository(
            @PathVariable String repositoryId,
            @RequestParam String provider) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        return gitService.getRepository(repositoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ======== Branch endpoints ========

    /**
     * Get branches for a repository
     */
    @GetMapping("/branches")
    public ResponseEntity<List<BranchDTO>> getBranches(
            @RequestParam String provider,
            @RequestParam String repositoryId) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        return ResponseEntity.ok(gitService.getBranches(repositoryId));
    }

    /**
     * Get a branch
     */
    @GetMapping("/branches/{branchName}")
    public ResponseEntity<BranchDTO> getBranch(
            @PathVariable String branchName,
            @RequestParam String provider,
            @RequestParam String repositoryId) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        return gitService.getBranch(repositoryId, branchName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a branch
     */
    @PostMapping("/branches")
    public ResponseEntity<BranchDTO> createBranch(
            @RequestParam String provider,
            @RequestParam String repositoryId,
            @RequestParam String branchName,
            @RequestParam String sourceBranch) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        BranchDTO branch = gitService.createBranch(repositoryId, branchName, sourceBranch);
        return new ResponseEntity<>(branch, HttpStatus.CREATED);
    }

    /**
     * Delete a branch
     */
    @DeleteMapping("/branches/{branchName}")
    public ResponseEntity<Map<String, Boolean>> deleteBranch(
            @PathVariable String branchName,
            @RequestParam String provider,
            @RequestParam String repositoryId) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        boolean deleted = gitService.deleteBranch(repositoryId, branchName);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        return ResponseEntity.ok(response);
    }

    // ======== Commit endpoints ========

    /**
     * Get commits for a repository
     */
    @GetMapping("/commits")
    public ResponseEntity<List<CommitDTO>> getCommits(
            @RequestParam String provider,
            @RequestParam String repositoryId,
            @RequestParam(required = false) String branch,
            @RequestParam(defaultValue = "10") int limit) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        return ResponseEntity.ok(gitService.getCommits(repositoryId, branch, limit));
    }

    /**
     * Get a commit
     */
    @GetMapping("/commits/{commitId}")
    public ResponseEntity<CommitDTO> getCommit(
            @PathVariable String commitId,
            @RequestParam String provider,
            @RequestParam String repositoryId) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        return gitService.getCommit(repositoryId, commitId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a commit
     */
    @PostMapping("/commits")
    public ResponseEntity<CommitDTO> createCommit(
            @RequestParam String provider,
            @RequestParam String repositoryId,
            @RequestParam String branch,
            @RequestParam String message,
            @RequestBody Map<String, String> files) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        CommitDTO commit = gitService.createCommit(repositoryId, branch, message, files);
        return new ResponseEntity<>(commit, HttpStatus.CREATED);
    }

    // ======== Pull/Merge Request endpoints ========

    /**
     * Get pull/merge requests for a repository
     */
    @GetMapping("/pull-requests")
    public ResponseEntity<List<PullRequestDTO>> getPullRequests(
            @RequestParam String provider,
            @RequestParam String repositoryId,
            @RequestParam(defaultValue = "open") String state) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        return ResponseEntity.ok(gitService.getPullRequests(repositoryId, state));
    }

    /**
     * Get a pull/merge request
     */
    @GetMapping("/pull-requests/{pullRequestId}")
    public ResponseEntity<PullRequestDTO> getPullRequest(
            @PathVariable String pullRequestId,
            @RequestParam String provider,
            @RequestParam String repositoryId) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        return gitService.getPullRequest(repositoryId, pullRequestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a pull/merge request
     */
    @PostMapping("/pull-requests")
    public ResponseEntity<PullRequestDTO> createPullRequest(
            @RequestParam String provider,
            @RequestParam String repositoryId,
            @RequestParam String title,
            @RequestParam String sourceBranch,
            @RequestParam String targetBranch,
            @RequestParam(required = false) String description) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        PullRequestDTO pullRequest = gitService.createPullRequest(
                repositoryId, title, sourceBranch, targetBranch, description);
        return new ResponseEntity<>(pullRequest, HttpStatus.CREATED);
    }

    /**
     * Update a pull/merge request state
     */
    @PutMapping("/pull-requests/{pullRequestId}")
    public ResponseEntity<PullRequestDTO> updatePullRequest(
            @PathVariable String pullRequestId,
            @RequestParam String provider,
            @RequestParam String repositoryId,
            @RequestParam String state) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        PullRequestDTO pullRequest = gitService.updatePullRequest(repositoryId, pullRequestId, state);
        return ResponseEntity.ok(pullRequest);
    }

    /**
     * Merge a pull/merge request
     */
    @PostMapping("/pull-requests/{pullRequestId}/merge")
    public ResponseEntity<Map<String, Boolean>> mergePullRequest(
            @PathVariable String pullRequestId,
            @RequestParam String provider,
            @RequestParam String repositoryId) {
        
        // Check if authenticated with the provider
        if (!authService.isAuthenticated(provider)) {
            throw new GitApiException("Not authenticated with provider: " + provider);
        }
        
        GitService gitService = gitServiceFactory.getService(provider);
        boolean merged = gitService.mergePullRequest(repositoryId, pullRequestId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("merged", merged);
        return ResponseEntity.ok(response);
    }
}
package com.gitintegration.api.controller;

import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;
import com.gitintegration.api.dto.RepositoryDTO;
import com.gitintegration.api.service.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/git")
@RequiredArgsConstructor
public class GitController {
    
    private final GitService gitService;
    
    @GetMapping("/repositories")
    public ResponseEntity<List<RepositoryDTO>> getRepositories() {
        return ResponseEntity.ok(gitService.getRepositories());
    }
    
    @GetMapping("/repositories/{repositoryId}")
    public ResponseEntity<RepositoryDTO> getRepository(@PathVariable String repositoryId) {
        return gitService.getRepository(repositoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/repositories/{repositoryId}/branches")
    public ResponseEntity<List<BranchDTO>> getBranches(@PathVariable String repositoryId) {
        return ResponseEntity.ok(gitService.getBranches(repositoryId));
    }
    
    @GetMapping("/repositories/{repositoryId}/commits")
    public ResponseEntity<List<CommitDTO>> getCommits(
            @PathVariable String repositoryId,
            @RequestParam(required = false) String branch,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(gitService.getCommits(repositoryId, branch, limit));
    }
    
    @GetMapping("/repositories/{repositoryId}/pulls")
    public ResponseEntity<List<PullRequestDTO>> getPullRequests(
            @PathVariable String repositoryId,
            @RequestParam(defaultValue = "open") String state) {
        return ResponseEntity.ok(gitService.getPullRequests(repositoryId, state));
    }
    
    @PostMapping("/repositories/{repositoryId}/branches")
    public ResponseEntity<BranchDTO> createBranch(
            @PathVariable String repositoryId,
            @RequestParam String branchName,
            @RequestParam String sourceBranch) {
        return ResponseEntity.ok(gitService.createBranch(repositoryId, branchName, sourceBranch));
    }
    
    @DeleteMapping("/repositories/{repositoryId}/branches/{branchName}")
    public ResponseEntity<Void> deleteBranch(
            @PathVariable String repositoryId,
            @PathVariable String branchName) {
        boolean deleted = gitService.deleteBranch(repositoryId, branchName);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/repositories/{repositoryId}/commits")
    public ResponseEntity<CommitDTO> createCommit(
            @PathVariable String repositoryId,
            @RequestParam String branch,
            @RequestParam String message,
            @RequestBody Map<String, String> files) {
        return ResponseEntity.ok(gitService.createCommit(repositoryId, branch, message, files));
    }
    
    @PostMapping("/repositories/{repositoryId}/pulls")
    public ResponseEntity<PullRequestDTO> createPullRequest(
            @PathVariable String repositoryId,
            @RequestParam String title,
            @RequestParam String sourceBranch,
            @RequestParam String targetBranch,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(gitService.createPullRequest(repositoryId, title, sourceBranch, targetBranch, description));
    }
    
    @PatchMapping("/repositories/{repositoryId}/pulls/{pullRequestId}")
    public ResponseEntity<PullRequestDTO> updatePullRequest(
            @PathVariable String repositoryId,
            @PathVariable String pullRequestId,
            @RequestParam String state) {
        return ResponseEntity.ok(gitService.updatePullRequest(repositoryId, pullRequestId, state));
    }
    
    @PostMapping("/repositories/{repositoryId}/pulls/{pullRequestId}/merge")
    public ResponseEntity<Void> mergePullRequest(
            @PathVariable String repositoryId,
            @PathVariable String pullRequestId) {
        boolean merged = gitService.mergePullRequest(repositoryId, pullRequestId);
        return merged ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
