package com.gitintegration.api.controller;

import com.gitintegration.api.auth.AuthenticationRequestDTO;
import com.gitintegration.api.service.GitService;
import com.gitintegration.api.service.AuthenticationService; // Added import
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier; // Added import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GitService gitService;
    @Qualifier("authenticationService") // Added @Qualifier
    private final AuthenticationService authService; // Added AuthenticationService dependency

    @PostMapping("/authenticate")
    public ResponseEntity<Boolean> authenticate(@RequestBody AuthenticationRequestDTO request) {
        boolean authenticated = authService.authenticate(request.getToken()); // Changed to use authService
        return ResponseEntity.ok(authenticated);
    }
}