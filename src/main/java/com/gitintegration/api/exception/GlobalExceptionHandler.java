package com.gitintegration.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Maps exceptions to appropriate HTTP responses.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles GitApiException.
     * 
     * @param ex The exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(GitApiException.class)
    public ResponseEntity<Map<String, Object>> handleGitApiException(GitApiException ex) {
        log.error("Git API exception: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode());
        body.put("error", "Git API Error");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.valueOf(ex.getStatusCode()));
    }
    
    /**
     * Handles MissingServletRequestParameterException.
     * 
     * @param ex The exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        log.error("Missing parameter: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Missing required parameter: " + ex.getParameterName());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles MethodArgumentTypeMismatchException.
     * 
     * @param ex The exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Invalid value for parameter: " + ex.getName());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles all other exceptions.
     * 
     * @param ex The exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred");
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
