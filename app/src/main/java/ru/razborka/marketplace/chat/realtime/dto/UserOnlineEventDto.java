package ru.razborka.marketplace.chat.realtime.dto;

import java.time.Instant;

public record UserOnlineEventDto(
        Long threadId,
        Long userId,
        boolean online,
        Instant at
) {
}
