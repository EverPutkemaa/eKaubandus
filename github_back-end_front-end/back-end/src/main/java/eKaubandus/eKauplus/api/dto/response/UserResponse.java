package eKaubandus.eKauplus.api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String profileImage;
    private LocalDateTime createdAt;
    private Boolean emailVerified;
    private List<String> roles;
}
