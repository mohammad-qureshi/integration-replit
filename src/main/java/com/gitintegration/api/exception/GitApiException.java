package com.gitintegration.api.exception;

/**
 * Custom exception for Git API related errors
 */
public class GitApiException extends RuntimeException {
    
    public GitApiException(String message) {
        super(message);
    }
    
    public GitApiException(String message, Throwable cause) {
        super(message, cause);
    }
}