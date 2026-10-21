package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.api.dto.request.LoginRequest;
import com.portfolio.agentguard.api.dto.response.AuthResponse;
import com.portfolio.agentguard.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OAuth2 / JWT Token Issuer for demo users and AI agents")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive scoped JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String tenantId = (request.tenantId() != null && !request.tenantId().isBlank())
            ? request.tenantId()
            : "tenant-corp-alpha";

        String role = "ADMIN";
        if (request.email().contains("dev")) {
            role = "USER";
        } else if (request.email().contains("finance") || request.email().contains("operator")) {
            role = "OPERATOR";
        } else if (request.email().contains("agent")) {
            role = "AGENT";
        } else if (request.email().contains("audit")) {
            role = "AUDITOR";
        }

        String token = jwtTokenProvider.generateToken(request.email(), tenantId, role);
        return ResponseEntity.ok(new AuthResponse(token, tenantId, request.email(), role, 86400));
    }
}
