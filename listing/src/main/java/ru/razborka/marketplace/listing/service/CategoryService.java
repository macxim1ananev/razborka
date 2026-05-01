package ru.razborka.marketplace.listing.service;

import ru.razborka.marketplace.listing.web.dto.CategoryAttributeDto;
import ru.razborka.marketplace.listing.web.dto.CategoryTreeNodeDto;

import java.util.List;

public interface CategoryService {
    List<CategoryTreeNodeDto> tree();

    List<CategoryAttributeDto> attributes(Long categoryId);
}
