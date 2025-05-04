package eKaubandus.eKauplus.api.controller;

import eKaubandus.eKauplus.api.dto.request.*;
import eKaubandus.eKauplus.api.dto.response.JwtResponse;
import eKaubandus.eKauplus.api.dto.response.MessageResponse;
import eKaubandus.eKauplus.api.entity.Role;
import eKaubandus.eKauplus.api.entity.User;
import eKaubandus.eKauplus.api.entity.UserActivity;
import eKaubandus.eKauplus.api.repository.RoleRepository;
import eKaubandus.eKauplus.api.repository.UserActivityRepository;
import eKaubandus.eKauplus.api.repository.UserRepository;
import eKaubandus.eKauplus.api.security.jwt.JwtUtils;
import eKaubandus.eKauplus.api.security.services.UserDetailsImpl;
import eKaubandus.eKauplus.api.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserActivityRepository userActivityRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Operation(summary = "Kasutaja sisselogimine",
            description = "Autentimise JWT tokeni saamise meetod")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edukas autentimine",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Vigased sisendandmed",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Vale kasutajanimi või parool")
    })

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request) {
        // Check if user exists
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User not found!"));
        }

        User user = userOptional.get();

        // Check if account is locked
        if (user.isAccountLocked()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Account is locked due to too many failed login attempts. Please contact support."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Update user's login information
            user.setLastLoginIp(request.getRemoteAddr());
            user.setLastLoginDevice(request.getHeader("User-Agent"));
            user.setLastLoginBrowser(extractBrowser(request.getHeader("User-Agent")));
            user.setLastLoginDate(LocalDateTime.now());
            user.setFailedLoginAttempts(0); // Reset failed attempts on successful login
            userRepository.save(user);

            // Log user activity
            UserActivity activity = new UserActivity();
            activity.setUser(user);
            activity.setActivityType(UserActivity.ActivityType.LOGIN);
            activity.setIpAddress(request.getRemoteAddr());
            activity.setDevice(request.getHeader("User-Agent"));
            activity.setBrowser(extractBrowser(request.getHeader("User-Agent")));
            activity.setDetails("User logged in successfully");
            activity.setSuccess(true);
            userActivityRepository.save(activity);

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    refreshToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    userDetails.getFullName(),
                    roles));

        } catch (Exception e) {
            // Increment failed login attempts
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // Lock account after 6 failed attempts
            if (user.getFailedLoginAttempts() >= 6) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }

            userRepository.save(user);

            // Log failed login attempt
            UserActivity activity = new UserActivity();
            activity.setUser(user);
            activity.setActivityType(UserActivity.ActivityType.LOGIN);
            activity.setIpAddress(request.getRemoteAddr());
            activity.setDevice(request.getHeader("User-Agent"));
            activity.setBrowser(extractBrowser(request.getHeader("User-Agent")));
            activity.setDetails("Failed login attempt: " + e.getMessage());
            activity.setSuccess(false);
            userActivityRepository.save(activity);

            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid username or password!"));
        }
    }

    @Operation(summary = "Kasutaja registreerimine",
            description = "Loob uue kasutaja konto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kasutaja edukalt registreeritud"),
            @ApiResponse(responseCode = "400", description = "Kasutaja eksisteerib juba")
    })

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest,
                                          HttpServletRequest request) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        user.setFullName(signUpRequest.getFullName());
        user.setPhone(signUpRequest.getPhone());
        user.setAddress(signUpRequest.getAddress());

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiryDate(LocalDateTime.now().plusDays(1));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(Role.ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Send verification email with token
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        // Log registration activity
        UserActivity activity = new UserActivity();
        activity.setUser(savedUser);
        activity.setActivityType(UserActivity.ActivityType.REGISTRATION);
        activity.setIpAddress(request.getRemoteAddr());
        activity.setDevice(request.getHeader("User-Agent"));
        activity.setBrowser(extractBrowser(request.getHeader("User-Agent")));
        activity.setDetails("User registered successfully");
        activity.setSuccess(true);
        userActivityRepository.save(activity);

        return ResponseEntity.ok(new MessageResponse("User registered successfully! Please check your email to verify your account."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Error: Invalid verification token."));

        if (user.getVerificationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Verification token has expired."));
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiryDate(null);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Email verified successfully. You can now login."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        User user = userRepository.findByEmail(resetRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: User not found with this email."));

        // Generate password reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        // Send password reset email with token
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        return ResponseEntity.ok(new MessageResponse("Password reset link has been sent to your email."));
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordUpdateRequest updateRequest) {
        User user = userRepository.findByResetPasswordToken(updateRequest.getToken())
                .orElseThrow(() -> new RuntimeException("Error: Invalid reset token."));

        if (user.getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Reset token has expired."));
        }

        user.setPassword(encoder.encode(updateRequest.getPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiryDate(null);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password updated successfully. You can now login with your new password."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam String token) {
        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid refresh token."));
        }

        String username = jwtUtils.getUserNameFromJwtToken(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        String newToken = jwtUtils.generateJwtToken(username);

        return ResponseEntity.ok(Map.of("token", newToken));
    }

    // Helper method to extract browser from User-Agent
    private String extractBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";

        if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Chrome")) {
            return "Chrome";
        } else if (userAgent.contains("Safari")) {
            return "Safari";
        } else if (userAgent.contains("Edge")) {
            return "Edge";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }
}
