package ru.razborka.marketplace.listing.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StartChatRequest(
        @NotNull Long listingId,
        @NotBlank @Size(max = 4000) String message
) {
}
