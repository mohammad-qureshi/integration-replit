package com.gitintegration.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Git branches.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a branch in a Git repository")
public class BranchDTO {
    
    @Schema(description = "Name of the branch", example = "main")
    private String name;
    
    @Schema(description = "SHA hash of the head commit", example = "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d")
    private String commitSha;
}
