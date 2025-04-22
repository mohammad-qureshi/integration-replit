package com.gitintegration.api.controller;

import com.gitintegration.api.auth.AuthenticationRequestDTO;
import com.gitintegration.api.service.GitService;
import com.gitintegration.api.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GitService gitService;
    private final AuthenticationService authService;

    public AuthController(GitService gitService, AuthenticationService authService) {
        this.gitService = gitService;
        this.authService = authService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Boolean> authenticate(@RequestBody AuthenticationRequestDTO request) {
        boolean authenticated = authService.authenticate(request.getToken());
        return ResponseEntity.ok(authenticated);
    }
}