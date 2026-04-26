package ru.razborka.marketplace.auth.telegram;

import org.springframework.stereotype.Component;
import ru.razborka.marketplace.common.exception.BusinessException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Проверка данных Telegram Login Widget: секретный ключ = SHA-256(bot_token) (32 байта), см. документацию Telegram.
 */
@Component
public class TelegramLoginVerifier {

    public Map<String, String> verifyAndParse(String botToken, Map<String, String> data) {
        if (botToken == null || botToken.isBlank()) {
            throw new BusinessException("CONFIG", "Не задан токен Telegram-бота");
        }
        Map<String, String> copy = new HashMap<>(data);
        String hash = copy.remove("hash");
        if (hash == null || hash.isBlank()) {
            throw new BusinessException("TELEGRAM_AUTH", "Отсутствует параметр hash");
        }
        List<String> keys = new ArrayList<>(copy.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            String k = keys.get(i);
            sb.append(k).append('=').append(copy.get(k));
        }
        String dataCheckString = sb.toString();
        byte[] secretKey = sha256(botToken.getBytes(StandardCharsets.UTF_8));
        String calculatedHex = hmacSha256Hex(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8));
        if (!calculatedHex.equalsIgnoreCase(hash)) {
            throw new BusinessException("TELEGRAM_AUTH", "Неверная подпись Telegram");
        }
        return copy;
    }

    public TelegramUserPayload extractUser(Map<String, String> verified) {
        long telegramId = Long.parseLong(required(verified, "id"));
        String firstName = verified.getOrDefault("first_name", "");
        String username = Optional.ofNullable(verified.get("username")).orElse("");
        String photoUrl = Optional.ofNullable(verified.get("photo_url")).orElse("");
        return new TelegramUserPayload(telegramId, firstName, username, photoUrl);
    }

    private static String required(Map<String, String> m, String key) {
        String v = m.get(key);
        if (v == null || v.isBlank()) {
            throw new BusinessException("TELEGRAM_AUTH", "Отсутствует поле: " + key);
        }
        return v;
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен", e);
        }
    }

    private static String hmacSha256Hex(byte[] secretKey, byte[] message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] raw = mac.doFinal(message);
            return toHex(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public record TelegramUserPayload(long telegramId, String firstName, String username, String photoUrl) {
    }
}
