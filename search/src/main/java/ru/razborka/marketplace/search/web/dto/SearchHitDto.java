package ru.razborka.marketplace.search.web.dto;

import java.math.BigDecimal;

public record SearchHitDto(
        Long id,
        String title,
        BigDecimal price,
        String firstPhotoUrl,
        String sellerCity,
        Integer vehicleYear,
        Integer mileageKm,
        String partCondition,
        String originalReplica,
        Long categoryId
) {
}
