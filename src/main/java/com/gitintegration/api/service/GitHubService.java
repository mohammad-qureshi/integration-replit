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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for GitHub API integration.
 * Implements methods to fetch branches, commits, and pull requests from GitHub repositories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubService {

    private final WebClient webClient;
    
    @Value("${github.api.token:}")
    private String githubToken;
    
    @Value("${github.api.base-url:https://api.github.com}")
    private String baseUrl;

    /**
     * Get all branches from a GitHub repository.
     * 
     * @param owner Repository owner
     * @param repo Repository name
     * @return List of branches
     */
    public List<BranchDTO> getBranches(String owner, String repo) {
        try {
            return webClient.get()
                    .uri(baseUrl + "/repos/{owner}/{repo}/branches", owner, repo)
                    .headers(this::setHeaders)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .map(branch -> {
                        String name = (String) branch.get("name");
                        Map<String, Object> commitMap = (Map<String, Object>) branch.get("commit");
                        String commitSha = (String) commitMap.get("sha");
                        return new BranchDTO(name, commitSha);
                    })
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error fetching branches from GitHub: {}", ex.getMessage());
            throw new GitApiException("Failed to fetch branches from GitHub", ex, ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Unexpected error fetching branches from GitHub: {}", ex.getMessage());
            throw new GitApiException("Unexpected error fetching branches from GitHub", ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Get commits from a GitHub repository.
     * 
     * @param owner Repository owner
     * @param repo Repository name
     * @param branch Branch name (optional)
     * @param limit Maximum number of commits to fetch
     * @return List of commits
     */
    public List<CommitDTO> getCommits(String owner, String repo, String branch, int limit) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/repos/{owner}/{repo}/commits")
                                .queryParam("per_page", limit);
                        
                        if (branch != null && !branch.isEmpty()) {
                            builder.queryParam("sha", branch);
                        }
                        
                        return builder.build(owner, repo);
                    })
                    .headers(this::setHeaders);
            
            return request.retrieve()
                    .bodyToFlux(Map.class)
                    .map(commit -> {
                        Map<String, Object> commitData = (Map<String, Object>) commit.get("commit");
                        String sha = (String) commit.get("sha");
                        String message = (String) commitData.get("message");
                        
                        Map<String, Object> authorData = (Map<String, Object>) commitData.get("author");
                        String authorName = (String) authorData.get("name");
                        String date = (String) authorData.get("date");
                        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
                        
                        return new CommitDTO(sha, message, authorName, dateTime);
                    })
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error fetching commits from GitHub: {}", ex.getMessage());
            throw new GitApiException("Failed to fetch commits from GitHub", ex, ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Unexpected error fetching commits from GitHub: {}", ex.getMessage());
            throw new GitApiException("Unexpected error fetching commits from GitHub", ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Get pull requests from a GitHub repository.
     * 
     * @param owner Repository owner
     * @param repo Repository name
     * @param state Pull request state (open, closed, all)
     * @return List of pull requests
     */
    public List<PullRequestDTO> getPullRequests(String owner, String repo, String state) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/pulls")
                            .queryParam("state", state)
                            .build(owner, repo))
                    .headers(this::setHeaders)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .map(pr -> {
                        Long id = ((Number) pr.get("id")).longValue();
                        Long number = ((Number) pr.get("number")).longValue();
                        String title = (String) pr.get("title");
                        String prState = (String) pr.get("state");
                        
                        Map<String, Object> userMap = (Map<String, Object>) pr.get("user");
                        String author = (String) userMap.get("login");
                        
                        String createdAt = (String) pr.get("created_at");
                        LocalDateTime createdDateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
                        
                        String sourceBranch = (String) pr.get("head").toString();
                        String targetBranch = (String) pr.get("base").toString();
                        
                        return new PullRequestDTO(id, number, title, author, prState, 
                                createdDateTime, sourceBranch, targetBranch);
                    })
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error fetching pull requests from GitHub: {}", ex.getMessage());
            throw new GitApiException("Failed to fetch pull requests from GitHub", ex, ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Unexpected error fetching pull requests from GitHub: {}", ex.getMessage());
            throw new GitApiException("Unexpected error fetching pull requests from GitHub", ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void setHeaders(HttpHeaders headers) {
        if (githubToken != null && !githubToken.isEmpty()) {
            headers.setBearerAuth(githubToken);
        }
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("User-Agent", "Git-Integration-API");
    }
}
