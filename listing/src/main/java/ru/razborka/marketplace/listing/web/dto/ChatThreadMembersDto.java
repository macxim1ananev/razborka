package ru.razborka.marketplace.listing.web.dto;

public record ChatThreadMembersDto(
        Long threadId,
        Long buyerId,
        Long sellerId
) {
}
