package com.gitintegration.api.controller;

import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;
import com.gitintegration.api.dto.RepositoryDTO;
import com.gitintegration.api.service.GitHubService;
import com.gitintegration.api.service.GitLabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unified REST Controller for Git API integrations.
 * Provides a common interface for both GitHub and GitLab repositories.
 */
@RestController
@RequestMapping("/api/git")
@RequiredArgsConstructor
@Tag(name = "Unified Git API", description = "Common endpoints for both GitHub and GitLab")
public class UnifiedGitController {

    private final GitHubService gitHubService;
    private final GitLabService gitLabService;

    @GetMapping("/branches")
    @Operation(summary = "List branches", description = "Fetches all branches using either GitHub or GitLab integration")
    public ResponseEntity<List<BranchDTO>> listBranches(
            @Parameter(description = "Git provider (github or gitlab)") @RequestParam String provider,
            @Parameter(description = "Repository identifier (owner/repo for GitHub, projectId for GitLab)") @RequestParam String repository) {
        
        if ("github".equalsIgnoreCase(provider)) {
            String[] parts = repository.split("/");
            if (parts.length != 2) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(gitHubService.getBranches(parts[0], parts[1]));
        } else if ("gitlab".equalsIgnoreCase(provider)) {
            return ResponseEntity.ok(gitLabService.getBranches(repository));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/commits")
    @Operation(summary = "List commits", description = "Fetches commits using either GitHub or GitLab integration")
    public ResponseEntity<List<CommitDTO>> listCommits(
            @Parameter(description = "Git provider (github or gitlab)") @RequestParam String provider,
            @Parameter(description = "Repository identifier (owner/repo for GitHub, projectId for GitLab)") @RequestParam String repository,
            @Parameter(description = "Branch name (optional)") @RequestParam(required = false) String branch,
            @Parameter(description = "Maximum number of commits to fetch") @RequestParam(defaultValue = "30") int limit) {
        
        if ("github".equalsIgnoreCase(provider)) {
            String[] parts = repository.split("/");
            if (parts.length != 2) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(gitHubService.getCommits(parts[0], parts[1], branch, limit));
        } else if ("gitlab".equalsIgnoreCase(provider)) {
            return ResponseEntity.ok(gitLabService.getCommits(repository, branch, limit));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pull-requests")
    @Operation(summary = "List pull/merge requests", description = "Fetches pull requests (GitHub) or merge requests (GitLab)")
    public ResponseEntity<List<PullRequestDTO>> listPullRequests(
            @Parameter(description = "Git provider (github or gitlab)") @RequestParam String provider,
            @Parameter(description = "Repository identifier (owner/repo for GitHub, projectId for GitLab)") @RequestParam String repository,
            @Parameter(description = "Request state (open/opened, closed, merged, all)") @RequestParam(defaultValue = "open") String state) {
        
        if ("github".equalsIgnoreCase(provider)) {
            String[] parts = repository.split("/");
            if (parts.length != 2) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(gitHubService.getPullRequests(parts[0], parts[1], state));
        } else if ("gitlab".equalsIgnoreCase(provider)) {
            // Map GitHub states to GitLab states if needed
            String gitlabState = state;
            if ("open".equals(state)) {
                gitlabState = "opened";
            }
            return ResponseEntity.ok(gitLabService.getMergeRequests(repository, gitlabState));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
