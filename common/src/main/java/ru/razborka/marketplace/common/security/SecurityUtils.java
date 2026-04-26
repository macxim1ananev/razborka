package ru.razborka.marketplace.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.razborka.marketplace.common.exception.BusinessException;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Long> currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        Object p = auth.getPrincipal();
        if (p instanceof Long id) {
            return Optional.of(id);
        }
        return Optional.empty();
    }

    public static long requireUserId() {
        return currentUserId().orElseThrow(() -> new BusinessException("UNAUTHORIZED", "Требуется авторизация"));
    }
}
