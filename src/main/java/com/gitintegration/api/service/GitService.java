package com.gitintegration.api.service;

import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;
import com.gitintegration.api.dto.RepositoryDTO;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Git services (GitHub, GitLab, etc.)
 * This follows the Strategy pattern, where different implementations
 * can be provided for different Git providers.
 */
public interface GitService {

    /**
     * Get provider identifier
     * @return the provider identifier string (e.g., "github", "gitlab")
     */
    String getProviderName();

    /**
     * Authenticate with the Git provider
     * @param token the authentication token
     * @return true if authentication was successful
     */
    boolean authenticate(String token);

    /**
     * Check if the service is authenticated
     * @return true if the service is authenticated
     */
    boolean isAuthenticated();

    /**
     * Get a list of repositories for the authenticated user
     * @return list of repositories
     */
    List<RepositoryDTO> getRepositories();

    /**
     * Get a specific repository by owner/name for GitHub or id for GitLab
     * @param repositoryId repository identifier (format depends on provider)
     * @return the repository if found
     */
    Optional<RepositoryDTO> getRepository(String repositoryId);

    /**
     * Get branches from a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @return list of branches
     */
    List<BranchDTO> getBranches(String repositoryId);

    /**
     * Get a specific branch from a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @param branchName branch name
     * @return the branch if found
     */
    Optional<BranchDTO> getBranch(String repositoryId, String branchName);

    /**
     * Create a new branch in a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @param branchName name of the branch to create
     * @param sourceBranchName name of the source branch
     * @return the created branch
     */
    BranchDTO createBranch(String repositoryId, String branchName, String sourceBranchName);

    /**
     * Delete a branch from a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @param branchName name of the branch to delete
     * @return true if deletion was successful
     */
    boolean deleteBranch(String repositoryId, String branchName);

    /**
     * Get commits from a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @param branchName optional branch name to filter commits
     * @param limit maximum number of commits to return
     * @return list of commits
     */
    List<CommitDTO> getCommits(String repositoryId, String branchName, int limit);

    /**
     * Get a specific commit from a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @param commitId commit identifier (SHA)
     * @return the commit if found
     */
    Optional<CommitDTO> getCommit(String repositoryId, String commitId);

    /**
     * Create a new commit in a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @param branchName branch name for the commit
     * @param message commit message
     * @param files files to change in the commit (path -> content)
     * @return the created commit
     */
    CommitDTO createCommit(String repositoryId, String branchName, String message, 
                           java.util.Map<String, String> files);

    /**
     * Get pull/merge requests from a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @param state filter by state (e.g., "open", "closed", "all")
     * @return list of pull/merge requests
     */
    List<PullRequestDTO> getPullRequests(String repositoryId, String state);

    /**
     * Get a specific pull/merge request from a repository
     * @param repositoryId repository identifier (format depends on provider)
     * @param pullRequestId pull/merge request identifier
     * @return the pull/merge request if found
     */
    Optional<PullRequestDTO> getPullRequest(String repositoryId, String pullRequestId);

    /**
     * Create a new pull/merge request
     * @param repositoryId repository identifier (format depends on provider)
     * @param title title of the pull/merge request
     * @param sourceBranch source branch
     * @param targetBranch target branch
     * @param description description of the pull/merge request
     * @return the created pull/merge request
     */
    PullRequestDTO createPullRequest(String repositoryId, String title, 
                                    String sourceBranch, String targetBranch, 
                                    String description);

    /**
     * Update a pull/merge request
     * @param repositoryId repository identifier (format depends on provider)
     * @param pullRequestId pull/merge request identifier
     * @param state new state for the pull/merge request
     * @return the updated pull/merge request
     */
    PullRequestDTO updatePullRequest(String repositoryId, String pullRequestId, 
                                    String state);

    /**
     * Merge a pull/merge request
     * @param repositoryId repository identifier (format depends on provider)
     * @param pullRequestId pull/merge request identifier
     * @return true if merge was successful
     */
    boolean mergePullRequest(String repositoryId, String pullRequestId);
}