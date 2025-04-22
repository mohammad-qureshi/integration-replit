package com.rishabh.fiveday.integration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rishabh.fiveday.integration.dto.BranchDTO;
import com.rishabh.fiveday.integration.dto.CommitDTO;
import com.rishabh.fiveday.integration.dto.PullRequestDTO;
import com.rishabh.fiveday.integration.service.GitHubService;

import java.util.List;

/**
 * REST Controller for GitHub API integration.
 * Provides endpoints to fetch branches, commits, and pull requests from GitHub repositories.
 */
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Tag(name = "GitHub API", description = "Endpoints for GitHub integration")
public class GitHubController {

    private final GitHubService gitHubService;

    @GetMapping("/branches")
    @Operation(summary = "List branches", description = "Fetches all branches from a GitHub repository")
    public ResponseEntity<List<BranchDTO>> listBranches(
            @Parameter(description = "GitHub repository owner") @RequestParam String owner,
            @Parameter(description = "GitHub repository name") @RequestParam String repo) {
        return ResponseEntity.ok(gitHubService.getBranches(owner, repo));
    }

    @GetMapping("/commits")
    @Operation(summary = "List commits", description = "Fetches commits from a GitHub repository")
    public ResponseEntity<List<CommitDTO>> listCommits(
            @Parameter(description = "GitHub repository owner") @RequestParam String owner,
            @Parameter(description = "GitHub repository name") @RequestParam String repo,
            @Parameter(description = "Branch name (optional)") @RequestParam(required = false) String branch,
            @Parameter(description = "Maximum number of commits to fetch") @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(gitHubService.getCommits(owner, repo, branch, limit));
    }

    @GetMapping("/pull-requests")
    @Operation(summary = "List pull requests", description = "Fetches pull requests from a GitHub repository")
    public ResponseEntity<List<PullRequestDTO>> listPullRequests(
            @Parameter(description = "GitHub repository owner") @RequestParam String owner,
            @Parameter(description = "GitHub repository name") @RequestParam String repo,
            @Parameter(description = "Pull request state (open, closed, all)") @RequestParam(defaultValue = "open") String state) {
        return ResponseEntity.ok(gitHubService.getPullRequests(owner, repo, state));
    }
}
