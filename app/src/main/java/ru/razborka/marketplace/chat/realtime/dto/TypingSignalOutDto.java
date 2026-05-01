package ru.razborka.marketplace.chat.realtime.dto;

import java.time.Instant;

public record TypingSignalOutDto(
        Long threadId,
        Long userId,
        boolean typing,
        Instant at
) {
}
