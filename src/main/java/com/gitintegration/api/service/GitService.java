package com.gitintegration.api.service;

import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;
import com.gitintegration.api.dto.RepositoryDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Common interface for Git provider services (GitHub, GitLab)
 * This follows the Strategy pattern - allows different git provider implementations
 * to be used interchangeably
 */
public interface GitService {
    
    /**
     * Get authenticated user's repositories from the Git provider
     * @return list of repositories
     */
    List<RepositoryDTO> getRepositories();
    
    /**
     * Get a specific repository
     * @param repositoryId repository identifier (differs by provider)
     * @return the repository if found
     */
    Optional<RepositoryDTO> getRepository(String repositoryId);
    
    /**
     * Get the name of the Git provider
     * @return provider name (e.g., "github", "gitlab")
     */
    String getProviderName();
    
    /**
     * Authenticate with the Git provider
     * @param token authentication token
     * @return true if authentication successful
     */
    boolean authenticate(String token);
    
    /**
     * Get branches for a repository
     * @param repositoryId repository identifier
     * @return list of branches
     */
    List<BranchDTO> getBranches(String repositoryId);
    
    /**
     * Get a specific branch
     * @param repositoryId repository identifier
     * @param branchName branch name
     * @return the branch if found
     */
    Optional<BranchDTO> getBranch(String repositoryId, String branchName);
    
    /**
     * Create a new branch
     * @param repositoryId repository identifier
     * @param branchName name for the new branch
     * @param sourceBranch source branch to create from
     * @return the created branch
     */
    BranchDTO createBranch(String repositoryId, String branchName, String sourceBranch);
    
    /**
     * Delete a branch
     * @param repositoryId repository identifier
     * @param branchName branch name to delete
     * @return true if deleted successfully
     */
    boolean deleteBranch(String repositoryId, String branchName);
    
    /**
     * Get commits for a repository branch
     * @param repositoryId repository identifier
     * @param branch optional branch name (default branch if not specified)
     * @param limit maximum number of commits to retrieve
     * @return list of commits
     */
    List<CommitDTO> getCommits(String repositoryId, String branch, int limit);
    
    /**
     * Get a specific commit
     * @param repositoryId repository identifier
     * @param commitId commit identifier
     * @return the commit if found
     */
    Optional<CommitDTO> getCommit(String repositoryId, String commitId);
    
    /**
     * Create a commit
     * @param repositoryId repository identifier
     * @param branch branch to commit to
     * @param message commit message
     * @param files map of file paths to file content
     * @return the created commit
     */
    CommitDTO createCommit(String repositoryId, String branch, String message, Map<String, String> files);
    
    /**
     * Get pull/merge requests for a repository
     * @param repositoryId repository identifier
     * @param state filter by state (open/closed/all)
     * @return list of pull/merge requests
     */
    List<PullRequestDTO> getPullRequests(String repositoryId, String state);
    
    /**
     * Get a specific pull/merge request
     * @param repositoryId repository identifier
     * @param pullRequestId pull/merge request identifier
     * @return the pull/merge request if found
     */
    Optional<PullRequestDTO> getPullRequest(String repositoryId, String pullRequestId);
    
    /**
     * Create a pull/merge request
     * @param repositoryId repository identifier
     * @param title pull/merge request title
     * @param sourceBranch source branch
     * @param targetBranch target branch
     * @param description optional description
     * @return the created pull/merge request
     */
    PullRequestDTO createPullRequest(String repositoryId, String title, String sourceBranch, 
                                   String targetBranch, String description);
    
    /**
     * Update a pull/merge request state
     * @param repositoryId repository identifier
     * @param pullRequestId pull/merge request identifier
     * @param state new state
     * @return the updated pull/merge request
     */
    PullRequestDTO updatePullRequest(String repositoryId, String pullRequestId, String state);
    
    /**
     * Merge a pull/merge request
     * @param repositoryId repository identifier
     * @param pullRequestId pull/merge request identifier
     * @return true if merged successfully
     */
    boolean mergePullRequest(String repositoryId, String pullRequestId);
    
    /**
     * Check if this service is authenticated
     * @return true if authenticated
     */
    boolean isAuthenticated();
    
    /**
     * Set the authentication token
     * @param token authentication token
     */
    void setAuthToken(String token);
}