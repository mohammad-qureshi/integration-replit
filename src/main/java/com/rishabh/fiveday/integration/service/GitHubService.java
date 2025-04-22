package com.rishabh.fiveday.integration.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.rishabh.fiveday.integration.dto.BranchDTO;
import com.rishabh.fiveday.integration.dto.CommitDTO;
import com.rishabh.fiveday.integration.dto.PullRequestDTO;
import com.rishabh.fiveday.integration.dto.RepositoryDTO;

/**
 * Interface for GitHub API operations
 */
public interface GitHubService extends GitService {
    
    /**
     * Parse a GitHub repository identifier
     * Format: "owner/repo"
     * @param repositoryId the repository identifier
     * @return owner and repo name
     */
    String[] parseRepositoryId(String repositoryId);
    
    /**
     * Get branches for a GitHub repository
     * @param owner repository owner
     * @param repo repository name
     * @return list of branches
     */
    List<BranchDTO> getBranches(String owner, String repo);
    
    /**
     * Get commits for a GitHub repository
     * @param owner repository owner
     * @param repo repository name
     * @param branch optional branch name
     * @param limit maximum number of commits to retrieve
     * @return list of commits
     */
    List<CommitDTO> getCommits(String owner, String repo, String branch, int limit);
    
    /**
     * Get pull requests for a GitHub repository
     * @param owner repository owner
     * @param repo repository name
     * @param state filter by state (open/closed/all)
     * @return list of pull requests
     */
    List<PullRequestDTO> getPullRequests(String owner, String repo, String state);
}