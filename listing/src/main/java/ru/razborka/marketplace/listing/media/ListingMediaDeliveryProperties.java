package ru.razborka.marketplace.listing.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Публичные URL фото объявлений: при включённом CDN значения из БД (или из индекса поиска)
 * переписываются на origin CDN. В БД по-прежнему хранится то, что вернул {@code StorageService}
 * (обычно полный URL приложения или относительный ключ объекта).
 */
@ConfigurationProperties(prefix = "app.media.delivery")
public class ListingMediaDeliveryProperties {

    /**
     * Публичный origin CDN без завершающего слеша, например {@code https://img.example.com}.
     * Пусто — клиентам отдаётся значение из БД без изменений.
     */
    private String publicBaseUrl = "";

    /**
     * Префиксы URL, которые нужно заменить на {@link #publicBaseUrl} (как в {@code listing_photos.photo_url}).
     * Проверяются по порядку; подходит первое совпадение с началом строки.
     */
    private List<String> rewriteSourcePrefixes = new ArrayList<>();

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl;
    }

    public List<String> getRewriteSourcePrefixes() {
        return rewriteSourcePrefixes;
    }

    public void setRewriteSourcePrefixes(List<String> rewriteSourcePrefixes) {
        this.rewriteSourcePrefixes = rewriteSourcePrefixes == null ? new ArrayList<>() : new ArrayList<>(rewriteSourcePrefixes);
    }
}
