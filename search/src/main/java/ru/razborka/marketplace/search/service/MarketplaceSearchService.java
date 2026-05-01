package ru.razborka.marketplace.search.service;

import ru.razborka.marketplace.search.web.dto.SearchResponseDto;

import java.math.BigDecimal;

public interface MarketplaceSearchService {
    SearchResponseDto search(SearchParams params);

    record SearchParams(
            Long categoryId,
            String brand,
            String model,
            String generation,
            String vin,
            String catalogBlock,
            Integer yearFrom,
            Integer yearTo,
            Double engineVolume,
            String partCondition,
            String originalReplica,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String city,
            String q,
            int page,
            int size
    ) {
        public String cacheKey() {
            return String.join("|",
                    String.valueOf(categoryId),
                    normalize(brand),
                    normalize(model),
                    normalize(generation),
                    normalize(vin),
                    normalize(catalogBlock),
                    String.valueOf(yearFrom),
                    String.valueOf(yearTo),
                    String.valueOf(engineVolume),
                    normalize(partCondition),
                    normalize(originalReplica),
                    String.valueOf(priceMin),
                    String.valueOf(priceMax),
                    normalize(city),
                    normalize(q),
                    String.valueOf(page),
                    String.valueOf(size)
            );
        }

        private static String normalize(String value) {
            if (value == null || value.isBlank()) {
                return "-";
            }
            return value.trim().toLowerCase();
        }
    }
}
