package ru.razborka.marketplace.listing.web.dto;

public record CategoryAttributeDto(
        Long id,
        String name,
        String slug,
        String dataType,
        boolean required,
        int sortOrder
) {
}
