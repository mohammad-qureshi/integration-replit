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

@Service
@Slf4j
public class GitLabServiceImpl implements GitService {

    private final WebClient webClient;
    private String token;
    private boolean authenticated = false;

    public GitLabServiceImpl(@Value("${gitlab.api.url:https://gitlab.com/api/v4}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "Git-Integration-API")
                .build();
    }

    @Override
    public String getProviderName() {
        return "gitlab";
    }

    @Override
    public boolean authenticate(String token) {
        this.token = token;
        try {
            webClient.get()
                    .uri("/user")
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            this.authenticated = true;
            return true;
        } catch (Exception e) {
            log.error("GitLab authentication failed: {}", e.getMessage());
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
            List<Map<String, Object>> projectList = webClient.get()
                    .uri("/projects?membership=true")
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<RepositoryDTO> repositories = new ArrayList<>();
            if (projectList != null) {
                for (Map<String, Object> project : projectList) {
                    repositories.add(mapToRepositoryDTO(project));
                }
            }
            return repositories;
        } catch (Exception e) {
            log.error("Failed to get GitLab repositories: {}", e.getMessage());
            throw new GitApiException("Failed to get GitLab repositories", e);
        }
    }

    @Override
    public Optional<RepositoryDTO> getRepository(String repositoryId) {
        try {
            Map<String, Object> projectData = webClient.get()
                    .uri("/projects/{id}", repositoryId)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (projectData != null) {
                return Optional.of(mapToRepositoryDTO(projectData));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get GitLab repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to get GitLab repository: " + repositoryId, e);
        }
    }

    @Override
    public List<BranchDTO> getBranches(String projectId) {
        try {
            List<Map<String, Object>> branchList = webClient.get()
                    .uri("/projects/{projectId}/repository/branches", projectId)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<BranchDTO> branches = new ArrayList<>();
            if (branchList != null) {
                for (Map<String, Object> branch : branchList) {
                    branches.add(mapToBranchDTO(branch, projectId));
                }
            }
            return branches;
        } catch (Exception e) {
            log.error("Failed to get GitLab branches: {}", e.getMessage());
            throw new GitApiException("Failed to get GitLab branches", e);
        }
    }

    @Override
    public Optional<BranchDTO> getBranch(String repositoryId, String branchName) {
        try {
            Map<String, Object> branchData = webClient.get()
                    .uri("/projects/{id}/repository/branches/{branch}", repositoryId, branchName)
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
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("branch", branchName);
            requestBody.put("ref", sourceBranchName);

            Map<String, Object> responseData = webClient.post()
                    .uri("/projects/{id}/repository/branches", repositoryId)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseData != null) {
                return mapToBranchDTO(responseData, repositoryId);
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
            webClient.delete()
                    .uri("/projects/{id}/repository/branches/{branch}", repositoryId, branchName)
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
    public List<CommitDTO> getCommits(String projectId, String branch, int limit) {
        try {
            List<Map<String, Object>> commitList = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projects/{projectId}/repository/commits")
                            .queryParam("ref_name", branch)
                            .queryParam("per_page", limit)
                            .build(projectId))
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<CommitDTO> commits = new ArrayList<>();
            if (commitList != null) {
                for (Map<String, Object> commit : commitList) {
                    commits.add(mapToCommitDTO(commit, projectId));
                }
            }
            return commits;
        } catch (Exception e) {
            log.error("Failed to get GitLab commits: {}", e.getMessage());
            throw new GitApiException("Failed to get GitLab commits", e);
        }
    }

    @Override
    public Optional<CommitDTO> getCommit(String repositoryId, String commitId) {
        try {
            Map<String, Object> commitData = webClient.get()
                    .uri("/projects/{id}/repository/commits/{sha}", repositoryId, commitId)
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
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("branch", branchName);
            requestBody.put("commit_message", message);
            requestBody.put("actions", prepareCommitActions(files));

            Map<String, Object> responseData = webClient.post()
                    .uri("/projects/{id}/repository/commits", repositoryId)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseData != null) {
                return mapToCommitDTO(responseData, repositoryId);
            }
            throw new GitApiException("Failed to create commit");
        } catch (Exception e) {
            log.error("Failed to create commit for repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to create commit: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> prepareCommitActions(Map<String, String> files) {
        List<Map<String, Object>> actions = new ArrayList<>();

        for (Map.Entry<String, String> entry : files.entrySet()) {
            Map<String, Object> action = new HashMap<>();
            action.put("action", "update"); 
            action.put("file_path", entry.getKey());
            action.put("content", entry.getValue());

            actions.add(action);
        }

        return actions;
    }

    @Override
    public List<PullRequestDTO> getPullRequests(String repositoryId, String state) {
        try {
            String gitlabState = mapToGitLabState(state);

            List<Map<String, Object>> mrList = webClient.get()
                    .uri("/projects/{id}/merge_requests?state={state}", repositoryId, gitlabState)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<PullRequestDTO> mergeRequests = new ArrayList<>();
            if (mrList != null) {
                for (Map<String, Object> mr : mrList) {
                    mergeRequests.add(mapToPullRequestDTO(mr, repositoryId));
                }
            }
            return mergeRequests;
        } catch (Exception e) {
            log.error("Failed to get merge requests for repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to get merge requests for repository: " + repositoryId, e);
        }
    }
    
    @Override
    public Optional<PullRequestDTO> getPullRequest(String repositoryId, String pullRequestId) {
        try {
            Map<String, Object> mrData = webClient.get()
                    .uri("/projects/{id}/merge_requests/{merge_request_iid}", repositoryId, pullRequestId)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (mrData != null) {
                return Optional.of(mapToPullRequestDTO(mrData, repositoryId));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get merge request {} for repository {}: {}", pullRequestId, repositoryId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public PullRequestDTO createPullRequest(String repositoryId, String title, String sourceBranch, String targetBranch, String description) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", title);
            requestBody.put("source_branch", sourceBranch);
            requestBody.put("target_branch", targetBranch);
            requestBody.put("description", description);

            Map<String, Object> responseData = webClient.post()
                    .uri("/projects/{id}/merge_requests", repositoryId)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseData != null) {
                return mapToPullRequestDTO(responseData, repositoryId);
            }
            throw new GitApiException("Failed to create merge request");
        } catch (Exception e) {
            log.error("Failed to create merge request for repository {}: {}", repositoryId, e.getMessage());
            throw new GitApiException("Failed to create merge request: " + e.getMessage(), e);
        }
    }

    @Override
    public PullRequestDTO updatePullRequest(String repositoryId, String pullRequestId, String state) {
        try {
            String gitlabState = mapToGitLabStateAction(state);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("state_event", gitlabState);

            Map<String, Object> responseData = webClient.put()
                    .uri("/projects/{id}/merge_requests/{merge_request_iid}", repositoryId, pullRequestId)
                    .headers(this::setAuthHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseData != null) {
                return mapToPullRequestDTO(responseData, repositoryId);
            }
            throw new GitApiException("Failed to update merge request");
        } catch (Exception e) {
            log.error("Failed to update merge request {} for repository {}: {}", pullRequestId, repositoryId, e.getMessage());
            throw new GitApiException("Failed to update merge request: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean mergePullRequest(String repositoryId, String pullRequestId) {
        try {
            webClient.put()
                    .uri("/projects/{id}/merge_requests/{merge_request_iid}/merge", repositoryId, pullRequestId)
                    .headers(this::setAuthHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return true;
        } catch (Exception e) {
            log.error("Failed to merge request {} for repository {}: {}", pullRequestId, repositoryId, e.getMessage());
            return false;
        }
    }

    private void setAuthHeader(HttpHeaders headers) {
        if (token != null && !token.isEmpty()) {
            headers.setBearerAuth(token);
        }
    }

    private RepositoryDTO mapToRepositoryDTO(Map<String, Object> repoData) {
        return RepositoryDTO.builder()
                .id(repoData.get("id").toString())
                .name((String) repoData.get("name"))
                .fullName((String) repoData.get("path_with_namespace"))
                .url((String) repoData.get("web_url"))
                .description((String) repoData.get("description"))
                .defaultBranch((String) repoData.get("default_branch"))
                .providerId(getProviderName())
                .build();
    }

    private BranchDTO mapToBranchDTO(Map<String, Object> branchData, String repositoryId) {
        Map<String, Object> commit = (Map<String, Object>) branchData.get("commit");
        return BranchDTO.builder()
                .name((String) branchData.get("name"))
                .commitSha((String) commit.get("id"))
                .repositoryId(repositoryId)
                .build();
    }

    private CommitDTO mapToCommitDTO(Map<String, Object> commitData, String repositoryId) {
        String timestamp = (String) commitData.get("created_at");
        LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);

        return CommitDTO.builder()
                .sha((String) commitData.get("id"))
                .message((String) commitData.get("message"))
                .author((String) commitData.get("author_name"))
                .timestamp(dateTime)
                .repositoryId(repositoryId)
                .build();
    }

    private PullRequestDTO mapToPullRequestDTO(Map<String, Object> mrData, String repositoryId) {
        Map<String, Object> author = (Map<String, Object>) mrData.get("author");

        String createdAt = (String) mrData.get("created_at");
        LocalDateTime dateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);

        String state = (String) mrData.get("state");
        if ("merged".equals(state)) {
            state = "merged";
        } else if ("closed".equals(state)) {
            state = "closed";
        } else {
            state = "open";
        }

        return PullRequestDTO.builder()
                .id(Long.valueOf(mrData.get("id").toString()))
                .number(Integer.valueOf(mrData.get("iid").toString()))
                .title((String) mrData.get("title"))
                .description((String) mrData.get("description"))
                .author((String) author.get("username"))
                .state(state)
                .createdAt(dateTime)
                .sourceBranch((String) mrData.get("source_branch"))
                .targetBranch((String) mrData.get("target_branch"))
                .repositoryId(repositoryId)
                .build();
    }

    private String mapToGitLabState(String state) {
        switch (state.toLowerCase()) {
            case "open":
                return "opened";
            case "closed":
                return "closed";
            case "merged":
                return "merged";
            case "all":
                return "all";
            default:
                return "opened";
        }
    }

    private String mapToGitLabStateAction(String state) {
        switch (state.toLowerCase()) {
            case "open":
                return "reopen";
            case "closed":
                return "close";
            default:
                return "reopen";
        }
    }
    
    private String parseRepositoryId(String repositoryId) {
        try {
            return repositoryId.replace("/", "%2F");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid GitLab project ID format");
        }
    }
}