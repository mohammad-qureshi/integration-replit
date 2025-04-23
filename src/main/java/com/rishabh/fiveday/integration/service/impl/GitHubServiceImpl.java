package com.rishabh.fiveday.integration.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.rishabh.fiveday.integration.dto.BranchDTO;
import com.rishabh.fiveday.integration.dto.CommitDTO;
import com.rishabh.fiveday.integration.dto.PullRequestDTO;
import com.rishabh.fiveday.integration.dto.RepositoryDTO;
import com.rishabh.fiveday.integration.exception.GitApiException;
import com.rishabh.fiveday.integration.service.GitService;

import lombok.extern.slf4j.Slf4j;

/**
 * GitHub implementation of the GitService interface
 */
@Service
@Slf4j
public class GitHubServiceImpl implements GitService {

    private final WebClient webClient;
    private String token;
    private boolean authenticated = false;

    public GitHubServiceImpl(@Value("${github.api.url:https://api.github.com}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "Git-Integration-API")
                .build();
    }

    @Override
    public String getProviderName() {
        return "github";
    }

    @Override
    public boolean authenticate(String token) {
        this.token = token;
        try {
            // Test authentication by getting user info
            webClient.get()
                    .uri("/user")
                    .headers(headers -> {
                        if (token != null && !token.isEmpty()) {
                            headers.setBearerAuth(token);
                        }
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            this.authenticated = true;
            return true;
        } catch (Exception e) {
            log.error("GitHub authentication failed: {}", e.getMessage());
            this.authenticated = false;
            return false;
        }
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    @Override
    public void setAuthToken(String token) {
        this.token = token;
        authenticate(token);
    }

    @Override
    public List<RepositoryDTO> getRepositories() {
        try {
            List<Map<String, Object>> repoList = webClient.get()
                    .uri("/user/repos")
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<RepositoryDTO> repositories = new ArrayList<>();
            if (repoList != null) {
                for (Map<String, Object> repo : repoList) {
                    repositories.add(mapToRepositoryDTO(repo));
                }
            }
            return repositories;
        } catch (Exception e) {
            log.error("Failed to get GitHub repositories: {}", e.getMessage());
            throw new GitApiException("Failed to get GitHub repositories", e);
        }
    }

    @Override
    public Optional<RepositoryDTO> getRepository(String repositoryId) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            Map<String, Object> repoData = webClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (repoData != null) {
                return Optional.of(mapToRepositoryDTO(repoData));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get GitHub repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to get GitHub repository: " + repositoryId, e);
        }
    }

    @Override
    public List<BranchDTO> getBranches(String repositoryId) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];
            return getBranches(owner, repo);
        } catch (Exception e) {
            log.error("Failed to get branches for repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to get branches for repository: " + repositoryId, e);
        }
    }
    
    private List<BranchDTO> getBranches(String owner, String repo) {
        try {
            String repositoryId = owner + "/" + repo;
            List<Map<String, Object>> branchList = webClient.get()
                    .uri("/repos/{owner}/{repo}/branches", owner, repo)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<BranchDTO> branches = new ArrayList<>();
            if (branchList != null) {
                for (Map<String, Object> branch : branchList) {
                    branches.add(mapToBranchDTO(branch, repositoryId));
                }
            }
            return branches;
        } catch (Exception e) {
            log.error("Failed to get branches for repository {}/{}: {}", owner, repo, e.getMessage());
            throw new GitApiException("Failed to get branches for repository: " + owner + "/" + repo, e);
        }
    }

    @Override
    public Optional<BranchDTO> getBranch(String repositoryId, String branchName) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            Map<String, Object> branchData = webClient.get()
                    .uri("/repos/{owner}/{repo}/branches/{branch}", owner, repo, branchName)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (branchData != null) {
                return Optional.of(mapToBranchDTO(branchData, repositoryId));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get branch {} for repository {}: {}", branchName, repositoryId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public BranchDTO createBranch(String repositoryId, String branchName, String sourceBranchName) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            // Get the SHA of the source branch
            Optional<BranchDTO> sourceBranch = getBranch(repositoryId, sourceBranchName);
            if (!sourceBranch.isPresent()) {
                throw new GitApiException("Source branch not found: " + sourceBranchName);
            }

            // Create a reference to the new branch
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ref", "refs/heads/" + branchName);
            requestBody.put("sha", sourceBranch.get().getCommitSha());

            Map<String, Object> responseData = webClient.post()
                    .uri("/repos/{owner}/{repo}/git/refs", owner, repo)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseData != null) {
                // Now get the branch details
                return getBranch(repositoryId, branchName).orElseThrow(
                        () -> new GitApiException("Failed to retrieve created branch: " + branchName));
            }
            throw new GitApiException("Failed to create branch: " + branchName);
        } catch (Exception e) {
            log.error("Failed to create branch {} for repository {}: {}", branchName, repositoryId, e.getMessage());
            throw new GitApiException("Failed to create branch: " + branchName, e);
        }
    }

    @Override
    public boolean deleteBranch(String repositoryId, String branchName) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            webClient.delete()
                    .uri("/repos/{owner}/{repo}/git/refs/heads/{branch}", owner, repo, branchName)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            return true;
        } catch (Exception e) {
            log.error("Failed to delete branch {} for repository {}: {}", branchName, repositoryId, e.getMessage());
            return false;
        }
    }

    @Override
    public List<CommitDTO> getCommits(String repositoryId, String branchName, int limit) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];
            return getCommits(owner, repo, branchName, limit);
        } catch (Exception e) {
            log.error("Failed to get commits for repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to get commits for repository: " + repositoryId, e);
        }
    }
    
    private List<CommitDTO> getCommits(String owner, String repo, String branchName, int limit) {
        try {
            String repositoryId = owner + "/" + repo;
            
            // Build URI with query parameters
            String uri = "/repos/{owner}/{repo}/commits?per_page={limit}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("owner", owner);
            uriVariables.put("repo", repo);
            uriVariables.put("limit", limit);

            if (branchName != null && !branchName.isEmpty()) {
                uri += "&sha={branch}";
                uriVariables.put("branch", branchName);
            }

            List<Map<String, Object>> commitList = webClient.get()
                    .uri(uri, uriVariables)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<CommitDTO> commits = new ArrayList<>();
            if (commitList != null) {
                for (Map<String, Object> commit : commitList) {
                    commits.add(mapToCommitDTO(commit, repositoryId));
                }
            }
            return commits;
        } catch (Exception e) {
            log.error("Failed to get commits for repository {}/{}: {}", owner, repo, e.getMessage());
            throw new GitApiException("Failed to get commits for repository: " + owner + "/" + repo, e);
        }
    }

    @Override
    public Optional<CommitDTO> getCommit(String repositoryId, String commitId) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            Map<String, Object> commitData = webClient.get()
                    .uri("/repos/{owner}/{repo}/commits/{commit_sha}", owner, repo, commitId)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (commitData != null) {
                return Optional.of(mapToCommitDTO(commitData, repositoryId));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get commit {} for repository {}: {}", commitId, repositoryId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public CommitDTO createCommit(String repositoryId, String branchName, String message, Map<String, String> files) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            // Get the latest commit SHA from the branch
            Optional<BranchDTO> branch = getBranch(repositoryId, branchName);
            if (!branch.isPresent()) {
                throw new GitApiException("Branch not found: " + branchName);
            }
            
            String baseSha = branch.get().getCommitSha();
            
            // For each file, create a blob and add to tree
            List<Map<String, Object>> treeItems = new ArrayList<>();
            
            for (Map.Entry<String, String> entry : files.entrySet()) {
                // Create blob
                Map<String, Object> blobRequest = new HashMap<>();
                blobRequest.put("content", entry.getValue());
                blobRequest.put("encoding", "utf-8");
                
                Map<String, Object> blobResponse = webClient.post()
                        .uri("/repos/{owner}/{repo}/git/blobs", owner, repo)
                        .headers(this::setAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(blobRequest)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                
                if (blobResponse == null) {
                    throw new GitApiException("Failed to create blob for file: " + entry.getKey());
                }
                
                // Add to tree
                Map<String, Object> treeItem = new HashMap<>();
                treeItem.put("path", entry.getKey());
                treeItem.put("mode", "100644"); // Regular file
                treeItem.put("type", "blob");
                treeItem.put("sha", blobResponse.get("sha"));
                
                treeItems.add(treeItem);
            }
            
            // Create tree
            Map<String, Object> treeRequest = new HashMap<>();
            treeRequest.put("base_tree", baseSha);
            treeRequest.put("tree", treeItems);
            
            Map<String, Object> treeResponse = webClient.post()
                    .uri("/repos/{owner}/{repo}/git/trees", owner, repo)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(treeRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (treeResponse == null) {
                throw new GitApiException("Failed to create tree");
            }
            
            // Create commit
            Map<String, Object> commitRequest = new HashMap<>();
            commitRequest.put("message", message);
            commitRequest.put("tree", treeResponse.get("sha"));
            commitRequest.put("parents", List.of(baseSha));
            
            Map<String, Object> commitResponse = webClient.post()
                    .uri("/repos/{owner}/{repo}/git/commits", owner, repo)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(commitRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (commitResponse == null) {
                throw new GitApiException("Failed to create commit");
            }
            
            // Update branch reference
            Map<String, Object> refRequest = new HashMap<>();
            refRequest.put("sha", commitResponse.get("sha"));
            refRequest.put("force", true);
            
            webClient.patch()
                    .uri("/repos/{owner}/{repo}/git/refs/heads/{branch}", owner, repo, branchName)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(refRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            // Return the new commit
            return getCommit(repositoryId, (String) commitResponse.get("sha"))
                    .orElseThrow(() -> new GitApiException("Failed to retrieve created commit"));
            
        } catch (Exception e) {
            log.error("Failed to create commit for repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to create commit: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PullRequestDTO> getPullRequests(String repositoryId, String state) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];
            return getPullRequests(owner, repo, state);
        } catch (Exception e) {
            log.error("Failed to get pull requests for repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to get pull requests for repository: " + repositoryId, e);
        }
    }
    
    private List<PullRequestDTO> getPullRequests(String owner, String repo, String state) {
        try {
            String repositoryId = owner + "/" + repo;
            
            List<Map<String, Object>> prList = webClient.get()
                    .uri("/repos/{owner}/{repo}/pulls?state={state}", owner, repo, state)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<PullRequestDTO> pullRequests = new ArrayList<>();
            if (prList != null) {
                for (Map<String, Object> pr : prList) {
                    pullRequests.add(mapToPullRequestDTO(pr, repositoryId));
                }
            }
            return pullRequests;
        } catch (Exception e) {
            log.error("Failed to get pull requests for repository {}/{}: {}", owner, repo, e.getMessage());
            throw new GitApiException("Failed to get pull requests for repository: " + owner + "/" + repo, e);
        }
    }

    @Override
    public Optional<PullRequestDTO> getPullRequest(String repositoryId, String pullRequestId) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            Map<String, Object> prData = webClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{pull_number}", owner, repo, pullRequestId)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (prData != null) {
                return Optional.of(mapToPullRequestDTO(prData, repositoryId));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get pull request {} for repository {}: {}", pullRequestId, repositoryId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public PullRequestDTO createPullRequest(String repositoryId, String title, String sourceBranch, String targetBranch, String description) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", title);
            requestBody.put("head", sourceBranch);
            requestBody.put("base", targetBranch);
            requestBody.put("body", description);

            Map<String, Object> responseData = webClient.post()
                    .uri("/repos/{owner}/{repo}/pulls", owner, repo)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseData != null) {
                return mapToPullRequestDTO(responseData, repositoryId);
            }
            throw new GitApiException("Failed to create pull request");
        } catch (Exception e) {
            log.error("Failed to create pull request for repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to create pull request: " + e.getMessage(), e);
        }
    }

    @Override
    public PullRequestDTO updatePullRequest(String repositoryId, String pullRequestId, String state) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("state", state);

            Map<String, Object> responseData = webClient.patch()
                    .uri("/repos/{owner}/{repo}/pulls/{pull_number}", owner, repo, pullRequestId)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseData != null) {
                return mapToPullRequestDTO(responseData, repositoryId);
            }
            throw new GitApiException("Failed to update pull request");
        } catch (Exception e) {
            log.error("Failed to update pull request {} for repository {}: {}", pullRequestId, repositoryId, e.getMessage());
            throw new GitApiException("Failed to update pull request: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean mergePullRequest(String repositoryId, String pullRequestId) {
        try {
            String[] parts = parseRepositoryId(repositoryId);
            String owner = parts[0];
            String repo = parts[1];

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merge_method", "merge");

            webClient.put()
                    .uri("/repos/{owner}/{repo}/pulls/{pull_number}/merge", owner, repo, pullRequestId)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return true;
        } catch (Exception e) {
            log.error("Failed to merge pull request {} for repository {}: {}", pullRequestId, repositoryId, e.getMessage());
            return false;
        }
    }

    // Helper methods
    private void setAuthHeader(HttpHeaders headers) {
        if (token != null && !token.isEmpty()) {
            headers.setBearerAuth(token);
        }
    }

    public String[] parseRepositoryId(String repositoryId) {
        String[] parts = repositoryId.split("___");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid GitHub repository ID format. Expected: 'owner/repo'");
        }
        return parts;
    }

    private RepositoryDTO mapToRepositoryDTO(Map<String, Object> repoData) {
        return RepositoryDTO.builder()
                .id(repoData.get("id").toString())
                .name((String) repoData.get("name"))
                .fullName((String) repoData.get("full_name"))
                .url((String) repoData.get("html_url"))
                .description((String) repoData.get("description"))
                .defaultBranch((String) repoData.get("default_branch"))
                .providerId(getProviderName())
                .build();
    }

    private BranchDTO mapToBranchDTO(Map<String, Object> branchData, String repositoryId) {
        Map<String, Object> commit = (Map<String, Object>) branchData.get("commit");
        return BranchDTO.builder()
                .name((String) branchData.get("name"))
                .commitSha((String) commit.get("sha"))
                .repositoryId(repositoryId)
                .build();
    }

    private CommitDTO mapToCommitDTO(Map<String, Object> commitData, String repositoryId) {
        Map<String, Object> commit = (Map<String, Object>) commitData.get("commit");
        Map<String, Object> author = (Map<String, Object>) commit.get("author");
        
        String timestamp = (String) author.get("date");
        LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
        
        return CommitDTO.builder()
                .sha((String) commitData.get("sha"))
                .message((String) commit.get("message"))
                .author((String) author.get("name"))
                .timestamp(dateTime)
                .repositoryId(repositoryId)
                .build();
    }

    private PullRequestDTO mapToPullRequestDTO(Map<String, Object> prData, String repositoryId) {
        Map<String, Object> head = (Map<String, Object>) prData.get("head");
        Map<String, Object> base = (Map<String, Object>) prData.get("base");
        Map<String, Object> user = (Map<String, Object>) prData.get("user");
        
        String createdAt = (String) prData.get("created_at");
        LocalDateTime dateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
        
        return PullRequestDTO.builder()
                .id(Long.valueOf(prData.get("id").toString()))
                .number(Integer.valueOf(prData.get("number").toString()))
                .title((String) prData.get("title"))
                .description((String) prData.get("body"))
                .author((String) user.get("login"))
                .state((String) prData.get("state"))
                .createdAt(dateTime)
                .sourceBranch((String) head.get("ref"))
                .targetBranch((String) base.get("ref"))
                .repositoryId(repositoryId)
                .build();
    }
}