package com.example.demo.auth.dto.res;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        UserInfo user
) {
    public record UserInfo(
            Long id,
            String email,
            String name,
            List<String> roles,
            List<String> authorities
    ) {
    }
}
