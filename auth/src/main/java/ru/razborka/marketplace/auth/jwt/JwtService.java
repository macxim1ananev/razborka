package ru.razborka.marketplace.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import ru.razborka.marketplace.common.exception.BusinessException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    public static final String CLAIM_TYPE = "typ";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(sha256(properties.getSecret().getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256", e);
        }
    }

    public String createAccessToken(long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getAccessTtlMinutes() * 60);
        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public IssuedRefresh createRefreshToken(long userId) {
        String jti = UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getRefreshTtlDays() * 24 * 60 * 60);
        String token = Jwts.builder()
                .subject(Long.toString(userId))
                .id(jti)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        return new IssuedRefresh(token, jti, exp);
    }

    public Optional<Long> parseAccessUserId(String token) {
        try {
            Claims claims = parse(token);
            if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (ExpiredJwtException e) {
            return Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public RefreshClaims parseRefresh(String token) {
        try {
            Claims claims = parse(token);
            if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new BusinessException("AUTH", "Неверный тип токена");
            }
            String jti = claims.getId();
            if (jti == null || jti.isBlank()) {
                throw new BusinessException("AUTH", "Нет jti в refresh");
            }
            long userId = Long.parseLong(claims.getSubject());
            Instant exp = claims.getExpiration().toInstant();
            return new RefreshClaims(userId, jti, exp);
        } catch (ExpiredJwtException e) {
            throw new BusinessException("AUTH", "Refresh истёк");
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException("AUTH", "Невалидный refresh");
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record IssuedRefresh(String token, String jti, Instant expiresAt) {
    }

    public record RefreshClaims(long userId, String jti, Instant expiresAt) {
    }
}
