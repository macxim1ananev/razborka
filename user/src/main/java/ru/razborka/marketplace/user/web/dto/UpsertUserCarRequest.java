package ru.razborka.marketplace.user.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpsertUserCarRequest(
        @NotBlank @Size(max = 255) String displayName,
        @NotBlank @Size(max = 100) String brand,
        @NotBlank @Size(max = 100) String model,
        @Size(max = 120) String generation,
        @Min(1950) @Max(2100) Integer year,
        @DecimalMin("0.6") @DecimalMax("12.0") BigDecimal engineVolume
) {
}
