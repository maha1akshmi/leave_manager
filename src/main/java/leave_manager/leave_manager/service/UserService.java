package leave_manager.leave_manager.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import leave_manager.leave_manager.dto.request.UpdateProfileRequest;
import leave_manager.leave_manager.dto.response.UserResponse;
import leave_manager.leave_manager.enums.Role;
import leave_manager.leave_manager.exception.ResourceNotFoundException;
import leave_manager.leave_manager.exception.UnauthorizedException;
import leave_manager.leave_manager.model.User;
import leave_manager.leave_manager.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ---- GET CURRENT USER ----
    public UserResponse getCurrentUser(String email) {
        User user = findUserByEmail(email);
        return UserResponse.from(user);
    }

    // ---- UPDATE OWN PROFILE ----
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDepartment())) {
            user.setDepartment(request.getDepartment());
        }
        // Update password only if a new one is provided
        if (StringUtils.hasText(request.getNewPassword())) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            log.info("Password updated for user id={}", user.getId());
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    // ---- GET ALL USERS (ADMIN) ----
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    // ---- GET USER BY ID (ADMIN/MANAGER) ----
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.from(user);
    }

    // ---- CHANGE USER ROLE (ADMIN ONLY) ----
    @Transactional
    public UserResponse changeUserRole(Long userId, Role newRole, String requestedByEmail) {
        User requestedBy = findUserByEmail(requestedByEmail);
        if (requestedBy.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can change user roles");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        log.info("Admin {} changing role of user {} from {} to {}", 
                requestedByEmail, userId, user.getRole(), newRole);

        user.setRole(newRole);
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    // ---- DEACTIVATE USER (ADMIN ONLY) ----
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User id={} deactivated", userId);
    }

    // ---- GET TEAM MEMBERS OF A MANAGER ----
    public List<UserResponse> getTeamMembers(String managerEmail) {
        User manager = findUserByEmail(managerEmail);
        return userRepository.findActiveTeamMembersByManagerId(manager.getId())
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    // ---- Internal helper ----
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
