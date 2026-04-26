package ru.razborka.marketplace.listing.web.dto;

import java.math.BigDecimal;

public record ListingPreviewDto(
        Long id,
        String title,
        BigDecimal price,
        String firstPhotoUrl,
        String sellerCity,
        Integer vehicleYear,
        Integer mileageKm
) {
}
