package com.gitintegration.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Git branches
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {
    private String name;
    private String commitSha;
    private boolean isProtected;
    private String repositoryId;
}