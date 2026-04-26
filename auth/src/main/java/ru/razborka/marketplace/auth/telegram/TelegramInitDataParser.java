package ru.razborka.marketplace.auth.telegram;

import org.springframework.stereotype.Component;
import ru.razborka.marketplace.common.exception.BusinessException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramInitDataParser {

    /**
     * Разбор query-string из виджета (key=value&...) или уже разобранной карты.
     */
    public Map<String, String> parseQueryString(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            throw new BusinessException("TELEGRAM_AUTH", "Пустой initData");
        }
        String normalized = queryString.trim();
        if (normalized.startsWith("?")) {
            normalized = normalized.substring(1);
        }
        Map<String, String> map = new HashMap<>();
        for (String pair : normalized.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = urlDecode(pair.substring(0, eq));
            String value = urlDecode(pair.substring(eq + 1));
            map.put(key, value);
        }
        if (map.isEmpty()) {
            throw new BusinessException("TELEGRAM_AUTH", "Не удалось разобрать initData");
        }
        return map;
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
