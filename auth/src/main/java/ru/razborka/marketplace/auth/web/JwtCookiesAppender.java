package ru.razborka.marketplace.auth.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import ru.razborka.marketplace.auth.jwt.JwtProperties;
import ru.razborka.marketplace.auth.service.AuthService.TokenPair;

import java.time.Duration;

@Component
public class JwtCookiesAppender {

    private final JwtProperties jwtProperties;

    public JwtCookiesAppender(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public void append(HttpServletResponse httpResponse, TokenPair pair) {
        if (!jwtProperties.getCookie().isEnabled()) {
            return;
        }
        JwtProperties.Cookie c = jwtProperties.getCookie();
        long accessMaxAgeSec = jwtProperties.getAccessTtlMinutes() * 60;
        ResponseCookie access = ResponseCookie.from(c.getAccessCookieName(), pair.accessToken())
                .httpOnly(true)
                .secure(c.isSecure())
                .sameSite(c.getSameSite())
                .path("/")
                .maxAge(Duration.ofSeconds(accessMaxAgeSec))
                .build();
        long refreshMaxAgeSec = jwtProperties.getRefreshTtlDays() * 24L * 60 * 60;
        ResponseCookie refresh = ResponseCookie.from(c.getRefreshCookieName(), pair.refreshToken())
                .httpOnly(true)
                .secure(c.isSecure())
                .sameSite(c.getSameSite())
                .path("/")
                .maxAge(Duration.ofSeconds(refreshMaxAgeSec))
                .build();
        httpResponse.addHeader("Set-Cookie", access.toString());
        httpResponse.addHeader("Set-Cookie", refresh.toString());
    }
}
