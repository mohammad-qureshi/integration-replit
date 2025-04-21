package com.gitintegration.api.service;

import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;

import java.util.List;

/**
 * Common interface for Git service implementations.
 * Defines methods for fetching branches, commits, and pull/merge requests.
 */
public interface GitService {
    
    /**
     * Get all branches from a repository.
     * 
     * @param repositoryId Repository identifier
     * @return List of branches
     */
    List<BranchDTO> getBranches(String repositoryId);
    
    /**
     * Get commits from a repository.
     * 
     * @param repositoryId Repository identifier
     * @param branch Branch name (optional)
     * @param limit Maximum number of commits to fetch
     * @return List of commits
     */
    List<CommitDTO> getCommits(String repositoryId, String branch, int limit);
    
    /**
     * Get pull/merge requests from a repository.
     * 
     * @param repositoryId Repository identifier
     * @param state Request state (e.g., open, closed)
     * @return List of pull/merge requests
     */
    List<PullRequestDTO> getPullRequests(String repositoryId, String state);
}
