package ru.razborka.marketplace.listing.web.dto;

public record VehicleGenerationDto(
        Long id,
        String name,
        Integer yearFrom,
        Integer yearTo
) {
}
