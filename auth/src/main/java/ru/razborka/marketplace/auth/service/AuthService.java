package ru.razborka.marketplace.auth.service;

import java.time.Instant;

public interface AuthService {
    TokenPair authenticateTelegram(String initData);

    TokenPair issueTokenPairForUserByTelegramId(long telegramId);

    TokenPair refresh(String refreshTokenRaw);

    record TokenPair(String accessToken, String refreshToken, Instant refreshExpiresAt) {
    }
}
