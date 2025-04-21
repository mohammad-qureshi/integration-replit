
package com.gitintegration.api.controller;

import com.gitintegration.api.auth.AuthenticationRequestDTO;
import com.gitintegration.api.service.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GitService gitService;

    @PostMapping("/authenticate")
    public ResponseEntity<Boolean> authenticate(@RequestBody AuthenticationRequestDTO request) {
        boolean authenticated = gitService.authenticate(request.getToken());
        return ResponseEntity.ok(authenticated);
    }
}
