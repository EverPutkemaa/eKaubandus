package eKaubandus.eKauplus.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, max = 120)
    private String password;
}
