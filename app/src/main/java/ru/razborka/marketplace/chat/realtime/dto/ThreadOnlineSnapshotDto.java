package ru.razborka.marketplace.chat.realtime.dto;

public record ThreadOnlineSnapshotDto(
        Long threadId,
        UserOnlineStateDto buyer,
        UserOnlineStateDto seller
) {
}
