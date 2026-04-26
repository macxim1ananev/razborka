package ru.razborka.marketplace.listing.web.dto;

public record SellerContactDto(
        Long id,
        String telegramUsername,
        String firstName,
        String city
) {
}
