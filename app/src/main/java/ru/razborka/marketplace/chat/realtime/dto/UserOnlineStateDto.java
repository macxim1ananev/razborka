package ru.razborka.marketplace.chat.realtime.dto;

public record UserOnlineStateDto(
        Long userId,
        boolean online
) {
}
