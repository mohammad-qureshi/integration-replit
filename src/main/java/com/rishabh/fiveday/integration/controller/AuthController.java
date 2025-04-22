package com.rishabh.fiveday.integration.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rishabh.fiveday.integration.auth.AuthenticationRequestDTO;
import com.rishabh.fiveday.integration.auth.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Qualifier("authenticationService")
    private final AuthenticationService authService;

    @PostMapping("/authenticate")
    public ResponseEntity<Boolean> authenticate(@RequestBody AuthenticationRequestDTO request) {
        boolean authenticated = authService.authenticate(request.getProvider(), request.getToken());
        return ResponseEntity.ok(authenticated);
    }
}