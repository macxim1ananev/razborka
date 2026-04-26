package ru.razborka.marketplace.search.web.dto;

import java.util.List;
import java.util.Map;

public record SearchResponseDto(
        List<SearchHitDto> content,
        long totalElements,
        int page,
        int size,
        Map<String, List<FacetBucketDto>> facets
) {
}
