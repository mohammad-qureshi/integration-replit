package com.rishabh.fiveday.integration.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rishabh.fiveday.integration.config.GitServiceFactory;
import com.rishabh.fiveday.integration.dto.BranchDTO;
import com.rishabh.fiveday.integration.dto.CommitDTO;
import com.rishabh.fiveday.integration.dto.PullRequestDTO;
import com.rishabh.fiveday.integration.dto.RepositoryDTO;
import com.rishabh.fiveday.integration.service.GitService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/git")
@RequiredArgsConstructor
public class GitController {
	
//    private final GitService gitService;
    private final GitServiceFactory gitServiceFactory;
    
    @GetMapping("/repositories")
    public ResponseEntity<List<RepositoryDTO>> getRepositories() {
        return ResponseEntity.ok(getGitService().getRepositories());
    }
    
    @GetMapping("/repositories/{repositoryId}")
    public ResponseEntity<RepositoryDTO> getRepository(@PathVariable String repositoryId) {
        return getGitService().getRepository(repositoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/repositories/{repositoryId}/branches")
    public ResponseEntity<List<BranchDTO>> getBranches(@PathVariable String repositoryId) {
        return ResponseEntity.ok(getGitService().getBranches(repositoryId));
    }
    
    @GetMapping("/repositories/{repositoryId}/commits")
    public ResponseEntity<List<CommitDTO>> getCommits(
            @PathVariable String repositoryId,
            @RequestParam(required = false) String branch,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(getGitService().getCommits(repositoryId, branch, limit));
    }
    
    @GetMapping("/repositories/{repositoryId}/pulls")
    public ResponseEntity<List<PullRequestDTO>> getPullRequests(
            @PathVariable String repositoryId,
            @RequestParam(defaultValue = "open") String state) {
        return ResponseEntity.ok(getGitService().getPullRequests(repositoryId, state));
    }
    
    @PostMapping("/repositories/{repositoryId}/branches")
    public ResponseEntity<BranchDTO> createBranch(
            @PathVariable String repositoryId,
            @RequestParam String branchName,
            @RequestParam String sourceBranch) {
        return ResponseEntity.ok(getGitService().createBranch(repositoryId, branchName, sourceBranch));
    }
    
    @DeleteMapping("/repositories/{repositoryId}/branches/{branchName}")
    public ResponseEntity<Void> deleteBranch(
            @PathVariable String repositoryId,
            @PathVariable String branchName) {
        boolean deleted = getGitService().deleteBranch(repositoryId, branchName);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/repositories/{repositoryId}/commits")
    public ResponseEntity<CommitDTO> createCommit(
            @PathVariable String repositoryId,
            @RequestParam String branch,
            @RequestParam String message,
            @RequestBody Map<String, String> files) {
        return ResponseEntity.ok(getGitService().createCommit(repositoryId, branch, message, files));
    }
    
    @PostMapping("/repositories/{repositoryId}/pulls")
    public ResponseEntity<PullRequestDTO> createPullRequest(
            @PathVariable String repositoryId,
            @RequestParam String title,
            @RequestParam String sourceBranch,
            @RequestParam String targetBranch,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(getGitService().createPullRequest(repositoryId, title, sourceBranch, targetBranch, description));
    }
    
    @PatchMapping("/repositories/{repositoryId}/pulls/{pullRequestId}")
    public ResponseEntity<PullRequestDTO> updatePullRequest(
            @PathVariable String repositoryId,
            @PathVariable String pullRequestId,
            @RequestParam String state) {
        return ResponseEntity.ok(getGitService().updatePullRequest(repositoryId, pullRequestId, state));
    }
    
    @PostMapping("/repositories/{repositoryId}/pulls/{pullRequestId}/merge")
    public ResponseEntity<Void> mergePullRequest(
            @PathVariable String repositoryId,
            @PathVariable String pullRequestId) {
        boolean merged = getGitService().mergePullRequest(repositoryId, pullRequestId);
        return merged ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
    
	private GitService getGitService() {
		return gitServiceFactory.getService();
	}
}
