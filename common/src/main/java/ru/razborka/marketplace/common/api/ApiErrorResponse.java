package ru.razborka.marketplace.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        String code,
        String message,
        Map<String, String> details
) {
    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message, Map.of());
    }

    public static ApiErrorResponse of(String code, String message, Map<String, String> details) {
        return new ApiErrorResponse(code, message, details == null ? Map.of() : Map.copyOf(details));
    }
}
