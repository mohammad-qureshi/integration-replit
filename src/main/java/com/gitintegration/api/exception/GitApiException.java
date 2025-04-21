package com.gitintegration.api.exception;

/**
 * Custom exception for Git API operations
 */
public class GitApiException extends RuntimeException {
    
    public GitApiException(String message) {
        super(message);
    }
    
    public GitApiException(String message, Throwable cause) {
        super(message, cause);
    }
}