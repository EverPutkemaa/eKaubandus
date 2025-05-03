package eKaubandus.eKauplus.api.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {

    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;

    public JwtResponse(String token, String refreshToken, Long id, String username, String email,
                       String fullName, List<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }
}
