package ru.razborka.marketplace.user.web.dto;

import java.time.Instant;

public record UserProfileDto(
        Long id,
        Long telegramId,
        String username,
        String firstName,
        String phone,
        String avatarUrl,
        String city,
        String bio,
        String status,
        Instant createdAt,
        Instant lastOnline
) {
}
