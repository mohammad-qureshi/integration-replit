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
import com.rishabh.fiveday.integration.service.GitLabService;

import java.util.List;

/**
 * REST Controller for GitLab API integration.
 * Provides endpoints to fetch branches, commits, and merge requests from GitLab repositories.
 */
@RestController
@RequestMapping("/api/gitlab")
@RequiredArgsConstructor
@Tag(name = "GitLab API", description = "Endpoints for GitLab integration")
public class GitLabController {

    private final GitLabService gitLabService;

    @GetMapping("/branches")
    @Operation(summary = "List branches", description = "Fetches all branches from a GitLab repository")
    public ResponseEntity<List<BranchDTO>> listBranches(
            @Parameter(description = "GitLab project ID") @RequestParam String projectId) {
        return ResponseEntity.ok(gitLabService.getBranches(projectId));
    }

    @GetMapping("/commits")
    @Operation(summary = "List commits", description = "Fetches commits from a GitLab repository")
    public ResponseEntity<List<CommitDTO>> listCommits(
            @Parameter(description = "GitLab project ID") @RequestParam String projectId,
            @Parameter(description = "Branch name (optional)") @RequestParam(required = false) String branch,
            @Parameter(description = "Maximum number of commits to fetch") @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(gitLabService.getCommits(projectId, branch, limit));
    }

    @GetMapping("/merge-requests")
    @Operation(summary = "List merge requests", description = "Fetches merge requests from a GitLab repository")
    public ResponseEntity<List<PullRequestDTO>> listMergeRequests(
            @Parameter(description = "GitLab project ID") @RequestParam String projectId,
            @Parameter(description = "Merge request state (opened, closed, merged, all)") @RequestParam(defaultValue = "opened") String state) {
        return ResponseEntity.ok(gitLabService.getMergeRequests(projectId, state));
    }
}
