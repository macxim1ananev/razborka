package ru.razborka.marketplace.listing.media;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Слой выдачи URL медиа клиенту: CDN, подписанные URL (расширяйте здесь), переписывание legacy-origin.
 */
@Service
public class ListingMediaDeliveryService {

    private final ListingMediaDeliveryProperties properties;

    public ListingMediaDeliveryService(ListingMediaDeliveryProperties properties) {
        this.properties = properties;
    }

    /**
     * @param storedValue значение {@code listing_photos.photo_url} или зеркало из поискового индекса
     * @return абсолютный URL для встраивания в HTML/клиент
     */
    public String toPublicUrl(String storedValue) {
        if (!StringUtils.hasText(storedValue)) {
            return "";
        }
        String raw = storedValue.trim();
        String cdn = trimTrailingSlashes(properties.getPublicBaseUrl().trim());
        if (!StringUtils.hasText(cdn)) {
            return raw;
        }
        for (String prefix : properties.getRewriteSourcePrefixes()) {
            if (!StringUtils.hasText(prefix)) {
                continue;
            }
            String p = trimTrailingSlashes(prefix.trim());
            if (raw.startsWith(p)) {
                String tail = raw.substring(p.length());
                if (!tail.startsWith("/")) {
                    tail = "/" + tail;
                }
                return cdn + tail;
            }
        }
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        String path = raw.startsWith("/") ? raw : "/" + raw;
        return cdn + path;
    }

    private static String trimTrailingSlashes(String s) {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '/') {
            end--;
        }
        return end == s.length() ? s : s.substring(0, end);
    }
}
