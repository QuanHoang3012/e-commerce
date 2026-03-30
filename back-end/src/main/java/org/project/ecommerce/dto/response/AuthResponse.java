package org.project.ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;
import org.project.ecommerce.constant.Role;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresIn;   // milliseconds
    private long refreshTokenExpiresIn;  // milliseconds

    private UUID userId;
    private String email;
    private String fullName;
    private Role role;
}
