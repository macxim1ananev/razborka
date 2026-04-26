package ru.razborka.marketplace.listing.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.listing.service.CategoryService;
import ru.razborka.marketplace.listing.web.dto.CategoryAttributeDto;
import ru.razborka.marketplace.listing.web.dto.CategoryTreeNodeDto;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public List<CategoryTreeNodeDto> tree() {
        return categoryService.tree();
    }

    @GetMapping("/{id}/attributes")
    public List<CategoryAttributeDto> attributes(@PathVariable Long id) {
        return categoryService.attributes(id);
    }
}
