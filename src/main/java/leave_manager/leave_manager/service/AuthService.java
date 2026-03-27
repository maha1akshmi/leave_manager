package leave_manager.leave_manager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import leave_manager.leave_manager.dto.request.LoginRequest;
import leave_manager.leave_manager.dto.request.RegisterRequest;
import leave_manager.leave_manager.dto.response.AuthResponse;
import leave_manager.leave_manager.exception.DuplicateEmailException;
import leave_manager.leave_manager.exception.ResourceNotFoundException;
import leave_manager.leave_manager.exception.UnauthorizedException;
import leave_manager.leave_manager.model.User;
import leave_manager.leave_manager.repository.UserRepository;
import leave_manager.leave_manager.security.JwtUtil;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ---- REGISTER ----
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        // Build User entity
        User.UserBuilder builder = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : leave_manager.leave_manager.enums.Role.EMPLOYEE)
                .department(request.getDepartment())
                .isActive(true);

        // Attach manager if managerId is provided
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with id: " + request.getManagerId()));
            builder.manager(manager);
        }

        User savedUser = userRepository.save(builder.build());
        log.info("User registered successfully: id={}, role={}", savedUser.getId(), savedUser.getRole());

        // Issue tokens immediately after registration
        String accessToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    // ---- LOGIN ----
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // This throws BadCredentialsException automatically if wrong — handled in GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Your account has been deactivated. Contact admin.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        log.info("Login successful for user id={}", user.getId());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ---- REFRESH TOKEN ----
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Refresh token is invalid or expired. Please login again.");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    // ---- Helper ----
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .build();
    }
}
