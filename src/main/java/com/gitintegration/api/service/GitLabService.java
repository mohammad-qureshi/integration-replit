package com.gitintegration.api.service;

import com.gitintegration.api.dto.BranchDTO;
import com.gitintegration.api.dto.CommitDTO;
import com.gitintegration.api.dto.PullRequestDTO;
import com.gitintegration.api.exception.GitApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service for GitLab API integration.
 * Implements methods to fetch branches, commits, and merge requests from GitLab repositories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitLabService {

    private final WebClient webClient;
    
    @Value("${gitlab.api.token:}")
    private String gitlabToken;
    
    @Value("${gitlab.api.base-url:https://gitlab.com/api/v4}")
    private String baseUrl;

    /**
     * Get all branches from a GitLab repository.
     * 
     * @param projectId GitLab project ID
     * @return List of branches
     */
    public List<BranchDTO> getBranches(String projectId) {
        try {
            return webClient.get()
                    .uri(baseUrl + "/projects/{projectId}/repository/branches", projectId)
                    .headers(this::setHeaders)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .map(branch -> {
                        String name = (String) branch.get("name");
                        Map<String, Object> commitMap = (Map<String, Object>) branch.get("commit");
                        String commitId = (String) commitMap.get("id");
                        return new BranchDTO(name, commitId);
                    })
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error fetching branches from GitLab: {}", ex.getMessage());
            throw new GitApiException("Failed to fetch branches from GitLab", ex, ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Unexpected error fetching branches from GitLab: {}", ex.getMessage());
            throw new GitApiException("Unexpected error fetching branches from GitLab", ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Get commits from a GitLab repository.
     * 
     * @param projectId GitLab project ID
     * @param branch Branch name (optional)
     * @param limit Maximum number of commits to fetch
     * @return List of commits
     */
    public List<CommitDTO> getCommits(String projectId, String branch, int limit) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/projects/{projectId}/repository/commits")
                                .queryParam("per_page", limit);
                        
                        if (branch != null && !branch.isEmpty()) {
                            builder.queryParam("ref_name", branch);
                        }
                        
                        return builder.build(projectId);
                    })
                    .headers(this::setHeaders);
            
            return request.retrieve()
                    .bodyToFlux(Map.class)
                    .map(commit -> {
                        String id = (String) commit.get("id");
                        String message = (String) commit.get("message");
                        String authorName = (String) commit.get("author_name");
                        String createdAt = (String) commit.get("created_at");
                        LocalDateTime dateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
                        
                        return new CommitDTO(id, message, authorName, dateTime);
                    })
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error fetching commits from GitLab: {}", ex.getMessage());
            throw new GitApiException("Failed to fetch commits from GitLab", ex, ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Unexpected error fetching commits from GitLab: {}", ex.getMessage());
            throw new GitApiException("Unexpected error fetching commits from GitLab", ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Get merge requests from a GitLab repository.
     * 
     * @param projectId GitLab project ID
     * @param state Merge request state (opened, closed, merged, all)
     * @return List of merge requests
     */
    public List<PullRequestDTO> getMergeRequests(String projectId, String state) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projects/{projectId}/merge_requests")
                            .queryParam("state", state)
                            .build(projectId))
                    .headers(this::setHeaders)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .map(mr -> {
                        Long id = ((Number) mr.get("id")).longValue();
                        Long iid = ((Number) mr.get("iid")).longValue();
                        String title = (String) mr.get("title");
                        String mrState = (String) mr.get("state");
                        
                        Map<String, Object> userMap = (Map<String, Object>) mr.get("author");
                        String author = (String) userMap.get("username");
                        
                        String createdAt = (String) mr.get("created_at");
                        LocalDateTime createdDateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
                        
                        String sourceBranch = (String) mr.get("source_branch");
                        String targetBranch = (String) mr.get("target_branch");
                        
                        return new PullRequestDTO(id, iid, title, author, mrState, 
                                createdDateTime, sourceBranch, targetBranch);
                    })
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error fetching merge requests from GitLab: {}", ex.getMessage());
            throw new GitApiException("Failed to fetch merge requests from GitLab", ex, ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Unexpected error fetching merge requests from GitLab: {}", ex.getMessage());
            throw new GitApiException("Unexpected error fetching merge requests from GitLab", ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void setHeaders(HttpHeaders headers) {
        if (gitlabToken != null && !gitlabToken.isEmpty()) {
            headers.set("PRIVATE-TOKEN", gitlabToken);
        }
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "Git-Integration-API");
    }
}
