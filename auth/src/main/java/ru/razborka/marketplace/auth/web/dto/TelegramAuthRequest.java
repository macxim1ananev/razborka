package ru.razborka.marketplace.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TelegramAuthRequest(
        @NotBlank String initData
) {
}
