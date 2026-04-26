package ru.razborka.marketplace.listing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razborka.marketplace.listing.domain.Category;
import ru.razborka.marketplace.listing.repository.CategoryAttributeRepository;
import ru.razborka.marketplace.listing.repository.CategoryRepository;
import ru.razborka.marketplace.listing.web.dto.CategoryAttributeDto;
import ru.razborka.marketplace.listing.web.dto.CategoryTreeNodeDto;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Collator CATEGORY_NAME_ORDER = Collator.getInstance(Locale.forLanguageTag("ru"));
    private static final String SPECIALIZED_ROOT_SLUG = "parts";

    private final CategoryRepository categoryRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;

    public CategoryService(CategoryRepository categoryRepository, CategoryAttributeRepository categoryAttributeRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeNodeDto> tree() {
        List<Category> all = categoryRepository.findAll();
        Map<Long, List<Category>> byParent = all.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));
        List<Category> roots = all.stream()
                .filter(c -> c.getParent() == null)
                .filter(c -> SPECIALIZED_ROOT_SLUG.equals(c.getSlug()))
                .sorted(Comparator.comparing(Category::getName, CATEGORY_NAME_ORDER))
                .toList();
        List<CategoryTreeNodeDto> out = new ArrayList<>();
        for (Category r : roots) {
            out.add(toNode(r, byParent));
        }
        return out;
    }

    private CategoryTreeNodeDto toNode(Category c, Map<Long, List<Category>> byParent) {
        List<Category> children = byParent.getOrDefault(c.getId(), List.of());
        children = new ArrayList<>(children);
        children.sort(Comparator.comparing(Category::getName, CATEGORY_NAME_ORDER));
        List<CategoryTreeNodeDto> childDtos = new ArrayList<>();
        for (Category ch : children) {
            childDtos.add(toNode(ch, byParent));
        }
        List<CategoryAttributeDto> attrs = categoryAttributeRepository.findByCategoryIdOrderBySortOrderAsc(c.getId()).stream()
                .map(a -> new CategoryAttributeDto(a.getId(), a.getName(), a.getSlug(), a.getDataType(), a.isRequired(), a.getSortOrder()))
                .toList();
        return new CategoryTreeNodeDto(c.getId(), c.getName(), c.getSlug(), c.getLevel(), childDtos, attrs);
    }

    @Transactional(readOnly = true)
    public List<CategoryAttributeDto> attributes(Long categoryId) {
        return categoryAttributeRepository.findByCategoryIdOrderBySortOrderAsc(categoryId).stream()
                .map(a -> new CategoryAttributeDto(a.getId(), a.getName(), a.getSlug(), a.getDataType(), a.isRequired(), a.getSortOrder()))
                .toList();
    }
}
