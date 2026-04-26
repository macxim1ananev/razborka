package ru.razborka.marketplace.listing.web.dto;

import java.util.List;

public record VehicleModelDto(
        Long id,
        String name,
        List<VehicleGenerationDto> generations
) {
}
