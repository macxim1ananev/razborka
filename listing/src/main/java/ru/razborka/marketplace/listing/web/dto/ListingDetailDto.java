package ru.razborka.marketplace.listing.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import ru.razborka.marketplace.listing.domain.CatalogBlock;

public record ListingDetailDto(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String condition,
        String originalReplica,
        String vin,
        CatalogBlock catalogBlock,
        String status,
        Integer vehicleYear,
        Integer mileageKm,
        Long categoryId,
        String categoryName,
        List<String> photoUrls,
        List<CompatibilityDto> compatibility,
        SellerContactDto seller,
        List<ListingPreviewDto> moreFromSeller,
        Instant createdAt,
        Instant updatedAt
) {
}
