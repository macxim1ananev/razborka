package ru.razborka.marketplace.user.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 20) String phone,
        @Size(max = 255) String city,
        @Size(max = 2000) String bio
) {
}
