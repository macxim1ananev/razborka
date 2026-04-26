package ru.razborka.marketplace.listing.web.dto;

import java.util.List;

public record CategoryTreeNodeDto(
        Long id,
        String name,
        String slug,
        Integer level,
        List<CategoryTreeNodeDto> children,
        List<CategoryAttributeDto> attributes
) {
}
