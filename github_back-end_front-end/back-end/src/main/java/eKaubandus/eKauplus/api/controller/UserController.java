package eKaubandus.eKauplus.api.controller;

import eKaubandus.eKauplus.api.dto.response.MessageResponse;
import eKaubandus.eKauplus.api.dto.response.UserResponse;
import eKaubandus.eKauplus.api.entity.User;
import eKaubandus.eKauplus.api.entity.UserActivity;
import eKaubandus.eKauplus.api.repository.UserActivityRepository;
import eKaubandus.eKauplus.api.repository.UserRepository;
import eKaubandus.eKauplus.api.security.services.UserDetailsImpl;
import eKaubandus.eKauplus.api.service.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private S3Service s3Service;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        UserResponse response = mapUserToResponse(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        UserResponse response = mapUserToResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody Map<String, Object> updates,
                                        HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        if (updates.containsKey("fullName")) {
            user.setFullName((String) updates.get("fullName"));
        }

        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }

        if (updates.containsKey("address")) {
            user.setAddress((String) updates.get("address"));
        }

        if (updates.containsKey("password")) {
            user.setPassword(encoder.encode((String) updates.get("password")));
        }

        User updatedUser = userRepository.save(user);

        // Log user activity
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setActivityType(UserActivity.ActivityType.PROFILE_UPDATE);
        activity.setIpAddress(request.getRemoteAddr());
        activity.setDevice(request.getHeader("User-Agent"));
        activity.setBrowser(extractBrowser(request.getHeader("User-Agent")));
        activity.setDetails("User profile updated");
        activity.setSuccess(true);
        userActivityRepository.save(activity);

        UserResponse response = mapUserToResponse(updatedUser);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/profile-image")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> uploadProfileImage(@PathVariable Long id,
                                                @RequestParam("file") MultipartFile file,
                                                HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        try {
            // Kustutame vana profiilipildi, kui see on olemas
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty() &&
                    user.getProfileImage().contains(".s3.")) {  // Kontrollime, et tegemist on S3 URL-iga
                s3Service.deleteFile(user.getProfileImage());
            }

            // Laadime uue pildi üles S3 bucket'isse
            String profileImageUrl = s3Service.uploadFile(file, "profile-images");

            // Uuendame kasutaja profiilipildi
            user.setProfileImage(profileImageUrl);
            userRepository.save(user);

            // Log user activity
            UserActivity activity = new UserActivity();
            activity.setUser(user);
            activity.setActivityType(UserActivity.ActivityType.PROFILE_UPDATE);
            activity.setIpAddress(request.getRemoteAddr());
            activity.setDevice(request.getHeader("User-Agent"));
            activity.setBrowser(extractBrowser(request.getHeader("User-Agent")));
            activity.setDetails("Profile image updated");
            activity.setSuccess(true);
            userActivityRepository.save(activity);

            return ResponseEntity.ok(new MessageResponse("Profile image uploaded successfully."));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to upload profile image: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/activities")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> getUserActivities(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        List<UserActivity> activities = userActivityRepository.findByUserId(id);

        return ResponseEntity.ok(activities);
    }

    // Helper methods
    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setAddress(user.getAddress());
        response.setProfileImage(user.getProfileImage());
        response.setCreatedAt(user.getCreatedAt());
        response.setEmailVerified(user.isEmailVerified());

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        response.setRoles(roles);

        return response;
    }

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
