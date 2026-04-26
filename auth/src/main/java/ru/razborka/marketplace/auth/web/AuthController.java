package ru.razborka.marketplace.auth.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.auth.service.AuthService;
import ru.razborka.marketplace.auth.service.AuthService.TokenPair;
import ru.razborka.marketplace.auth.web.dto.AuthResponse;
import ru.razborka.marketplace.auth.web.dto.RefreshRequest;
import ru.razborka.marketplace.auth.web.dto.TelegramAuthRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtCookiesAppender jwtCookiesAppender;

    public AuthController(AuthService authService, JwtCookiesAppender jwtCookiesAppender) {
        this.authService = authService;
        this.jwtCookiesAppender = jwtCookiesAppender;
    }

    @PostMapping("/telegram")
    public ResponseEntity<AuthResponse> telegram(
            @Valid @RequestBody TelegramAuthRequest request,
            HttpServletResponse httpResponse
    ) {
        TokenPair pair = authService.authenticateTelegram(request.initData());
        jwtCookiesAppender.append(httpResponse, pair);
        return ResponseEntity.ok(AuthResponse.of(pair.accessToken(), pair.refreshToken(), pair.refreshExpiresAt()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletResponse httpResponse
    ) {
        TokenPair pair = authService.refresh(request.refreshToken());
        jwtCookiesAppender.append(httpResponse, pair);
        return ResponseEntity.ok(AuthResponse.of(pair.accessToken(), pair.refreshToken(), pair.refreshExpiresAt()));
    }
}
