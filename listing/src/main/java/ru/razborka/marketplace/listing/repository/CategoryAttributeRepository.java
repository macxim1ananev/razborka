package ru.razborka.marketplace.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.razborka.marketplace.listing.domain.CategoryAttribute;

import java.util.List;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {

    List<CategoryAttribute> findByCategoryIdOrderBySortOrderAsc(Long categoryId);
}
