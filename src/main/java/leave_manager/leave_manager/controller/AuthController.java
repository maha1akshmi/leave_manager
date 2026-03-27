package leave_manager.leave_manager.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import leave_manager.leave_manager.dto.request.LoginRequest;
import leave_manager.leave_manager.dto.request.RegisterRequest;
import leave_manager.leave_manager.dto.response.AuthResponse;
import leave_manager.leave_manager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, Login, Token management")
public class AuthController {

    private final AuthService authService;

    // ---- POST /api/auth/register ----
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new employee/manager/admin account and returns JWT tokens")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---- POST /api/auth/login ----
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email & password — returns access token + refresh token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ---- POST /api/auth/refresh ----
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Pass refresh token in body to get a new access token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    // ---- POST /api/auth/logout ----
    // With stateless JWT, logout is handled client-side by deleting the token.
    // Server just returns 200 OK to confirm.
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Stateless logout — client should delete the stored JWT token")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully. Please delete your token on the client."
        ));
    }
}
