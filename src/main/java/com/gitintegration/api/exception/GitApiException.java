package com.gitintegration.api.exception;

import lombok.Getter;

/**
 * Custom exception for Git API related errors.
 */
@Getter
public class GitApiException extends RuntimeException {
    
    private final int statusCode;
    
    /**
     * Constructs a new GitApiException with the specified detail message and status code.
     * 
     * @param message The detail message
     * @param statusCode The HTTP status code
     */
    public GitApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Constructs a new GitApiException with the specified detail message, cause, and status code.
     * 
     * @param message The detail message
     * @param cause The cause
     * @param statusCode The HTTP status code
     */
    public GitApiException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
