package ru.razborka.marketplace.listing.web.dto;

import java.math.BigDecimal;

public record CompatibilityDto(
        Long id,
        String brand,
        String model,
        String generation,
        Integer yearFrom,
        Integer yearTo,
        BigDecimal engineVolume
) {
}
