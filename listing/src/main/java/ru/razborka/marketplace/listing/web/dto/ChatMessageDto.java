package ru.razborka.marketplace.listing.web.dto;

import java.time.Instant;

public record ChatMessageDto(
        Long id,
        Long threadId,
        Long senderId,
        String senderFirstName,
        String senderUsername,
        String body,
        Instant createdAt,
        Instant readAt
) {
}
