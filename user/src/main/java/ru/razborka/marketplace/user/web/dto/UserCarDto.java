package ru.razborka.marketplace.user.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UserCarDto(
        Long id,
        String displayName,
        String brand,
        String model,
        String generation,
        Integer year,
        BigDecimal engineVolume,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
