package ru.razborka.marketplace.auth.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.auth.service.AuthService;
import ru.razborka.marketplace.auth.service.AuthService.TokenPair;
import ru.razborka.marketplace.auth.web.dto.AuthResponse;

/**
 * Только при {@code spring.profiles.active=dev|docker}: выдача пары JWT для заранее засеянного пользователя (см. Flyway V3).
 */
@RestController
@RequestMapping("/auth/dev")
@Profile({"dev", "docker"})
public class DevAuthController {

    public static final long DEFAULT_TEST_TELEGRAM_ID = 999_000_001L;

    private final AuthService authService;
    private final JwtCookiesAppender jwtCookiesAppender;

    public DevAuthController(AuthService authService, JwtCookiesAppender jwtCookiesAppender) {
        this.authService = authService;
        this.jwtCookiesAppender = jwtCookiesAppender;
    }

    /**
     * @param telegramId по умолчанию 999000001 ({@link #DEFAULT_TEST_TELEGRAM_ID}), также доступен 999000002.
     */
    @PostMapping("/token")
    public ResponseEntity<AuthResponse> issueToken(
            @RequestParam(name = "telegramId", required = false) Long telegramId,
            HttpServletResponse httpResponse
    ) {
        long tid = telegramId == null ? DEFAULT_TEST_TELEGRAM_ID : telegramId;
        TokenPair pair = authService.issueTokenPairForUserByTelegramId(tid);
        jwtCookiesAppender.append(httpResponse, pair);
        return ResponseEntity.ok(AuthResponse.of(pair.accessToken(), pair.refreshToken(), pair.refreshExpiresAt()));
    }
}
