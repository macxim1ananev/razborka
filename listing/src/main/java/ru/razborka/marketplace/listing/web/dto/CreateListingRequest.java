package ru.razborka.marketplace.listing.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.razborka.marketplace.listing.domain.CatalogBlock;

import java.math.BigDecimal;
import java.util.List;

public record CreateListingRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 10000) String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Pattern(regexp = "used|new|refurbished") String condition,
        @Pattern(regexp = "original|replica|oem") String originalReplica,
        @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$") String vin,
        CatalogBlock catalogBlock,
        Integer mileageKm,
        Integer vehicleYear,
        List<CompatibilityWriteDto> compatibility
) {
}
