package com.gitintegration.api.service;

import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;
import com.gitintegration.api.dto.RepositoryDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for GitLab API operations
 */
public interface GitLabService extends GitService {
    
    /**
     * Parse a GitLab repository identifier
     * Format: "project_id"
     * @param repositoryId the repository identifier (project id)
     * @return project id
     */
    String parseRepositoryId(String repositoryId);
    
    /**
     * Get merge requests from a GitLab project
     * @param projectId GitLab project ID
     * @param state filter by state (opened/closed/merged/all)
     * @return list of merge requests
     */
    List<PullRequestDTO> getMergeRequests(String projectId, String state);
}