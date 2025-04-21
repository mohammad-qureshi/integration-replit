package com.gitintegration.api.service;

import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;
import com.gitintegration.api.dto.RepositoryDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
}