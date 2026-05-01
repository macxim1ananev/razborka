package ru.razborka.marketplace.listing.web.dto;

import java.time.Instant;

public record ChatThreadDto(
        Long threadId,
        Long listingId,
        String listingTitle,
        Long peerUserId,
        String peerFirstName,
        String peerUsername,
        String lastMessage,
        Instant lastMessageAt,
        long unreadCount
) {
}
