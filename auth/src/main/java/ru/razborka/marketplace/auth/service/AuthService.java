package ru.razborka.marketplace.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razborka.marketplace.auth.config.TelegramBotProperties;
import ru.razborka.marketplace.auth.domain.RefreshToken;
import ru.razborka.marketplace.auth.jwt.JwtService;
import ru.razborka.marketplace.auth.jwt.JwtService.IssuedRefresh;
import ru.razborka.marketplace.auth.jwt.JwtService.RefreshClaims;
import ru.razborka.marketplace.auth.repository.RefreshTokenRepository;
import ru.razborka.marketplace.auth.telegram.TelegramInitDataParser;
import ru.razborka.marketplace.auth.telegram.TelegramLoginVerifier;
import ru.razborka.marketplace.auth.telegram.TelegramLoginVerifier.TelegramUserPayload;
import ru.razborka.marketplace.common.exception.BusinessException;
import ru.razborka.marketplace.common.exception.NotFoundException;
import ru.razborka.marketplace.user.domain.User;
import ru.razborka.marketplace.user.domain.UserStatus;
import ru.razborka.marketplace.user.repository.UserRepository;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {

    private final TelegramBotProperties telegramBotProperties;
    private final TelegramInitDataParser initDataParser;
    private final TelegramLoginVerifier telegramLoginVerifier;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public AuthService(
            TelegramBotProperties telegramBotProperties,
            TelegramInitDataParser initDataParser,
            TelegramLoginVerifier telegramLoginVerifier,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService
    ) {
        this.telegramBotProperties = telegramBotProperties;
        this.initDataParser = initDataParser;
        this.telegramLoginVerifier = telegramLoginVerifier;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenPair authenticateTelegram(String initData) {
        Map<String, String> raw = initDataParser.parseQueryString(initData);
        Map<String, String> verified = telegramLoginVerifier.verifyAndParse(telegramBotProperties.getBotToken(), raw);
        TelegramUserPayload tg = telegramLoginVerifier.extractUser(verified);
        User user = userRepository.findByTelegramId(tg.telegramId()).orElseGet(User::new);
        user.setTelegramId(tg.telegramId());
        user.setFirstName(tg.firstName());
        if (tg.username() != null && !tg.username().isBlank()) {
            user.setUsername(tg.username());
        }
        if (tg.photoUrl() != null && !tg.photoUrl().isBlank()) {
            user.setAvatarUrl(tg.photoUrl());
        }
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }
        user.setLastOnline(Instant.now());
        user = userRepository.save(user);
        return issueTokens(user.getId());
    }

    /**
     * Выдача JWT без Telegram — только для локальной разработки (профиль {@code dev}).
     */
    @Transactional
    public TokenPair issueTokenPairForUserByTelegramId(long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new NotFoundException("Пользователь с telegram_id=" + telegramId + " не найден"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("AUTH", "Пользователь неактивен");
        }
        user.setLastOnline(Instant.now());
        userRepository.save(user);
        return issueTokens(user.getId());
    }

    @Transactional
    public TokenPair refresh(String refreshTokenRaw) {
        RefreshClaims claims = jwtService.parseRefresh(refreshTokenRaw);
        RefreshToken stored = refreshTokenRepository.findByJtiAndRevokedFalse(claims.jti())
                .orElseThrow(() -> new BusinessException("AUTH", "Refresh не найден или отозван"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("AUTH", "Refresh истёк");
        }
        if (!stored.getUser().getId().equals(claims.userId())) {
            throw new BusinessException("AUTH", "Несоответствие пользователя");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(claims.userId());
    }

    private TokenPair issueTokens(long userId) {
        String access = jwtService.createAccessToken(userId);
        IssuedRefresh issued = jwtService.createRefreshToken(userId);
        RefreshToken entity = new RefreshToken();
        entity.setJti(issued.jti());
        User u = userRepository.getReferenceById(userId);
        entity.setUser(u);
        entity.setExpiresAt(issued.expiresAt());
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);
        return new TokenPair(access, issued.token(), issued.expiresAt());
    }

    public record TokenPair(String accessToken, String refreshToken, Instant refreshExpiresAt) {
    }
}
