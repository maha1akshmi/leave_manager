package leave_manager.leave_manager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import leave_manager.leave_manager.dto.request.UpdateProfileRequest;
import leave_manager.leave_manager.dto.response.UserResponse;
import leave_manager.leave_manager.enums.Role;
import leave_manager.leave_manager.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Profile management and admin user operations")
public class UserController {

    private final UserService userService;

    // ---- GET /api/users/me ----
    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Returns the profile of the currently authenticated user")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ---- PUT /api/users/me ----
    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update name, department, or password")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    // ---- GET /api/users — ADMIN ONLY ----
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)", description = "Returns list of all users in the system")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ---- GET /api/users/{id} — ADMIN or MANAGER ----
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get user by ID", description = "Admin or Manager can look up any user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ---- PUT /api/users/{id}/role — ADMIN ONLY ----
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change user role (Admin only)", description = "Promotes or demotes a user's role")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String roleStr = body.get("role");
        Role newRole;
        try {
            newRole = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return ResponseEntity.badRequest().build();
        }

        UserResponse response = userService.changeUserRole(id, newRole, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ---- GET /api/users/team — MANAGER sees their direct reports ----
    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get my team members", description = "Returns list of employees reporting to the current manager")
    public ResponseEntity<List<UserResponse>> getMyTeam(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getTeamMembers(userDetails.getUsername()));
    }
}
