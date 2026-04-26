package ru.razborka.marketplace.listing.web.dto;

import java.util.List;

public record VehicleMakeDto(
        Long id,
        String name,
        List<VehicleModelDto> models
) {
}
