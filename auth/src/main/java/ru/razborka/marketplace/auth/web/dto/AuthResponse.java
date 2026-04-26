package ru.razborka.marketplace.auth.web.dto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Instant refreshExpiresAt,
        String tokenType
) {
    public static AuthResponse of(String access, String refresh, Instant refreshExp) {
        return new AuthResponse(access, refresh, refreshExp, "Bearer");
    }
}
