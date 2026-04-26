package ru.razborka.marketplace.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.razborka.marketplace.listing.domain.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.attributes WHERE c.parent IS NULL")
    List<Category> findRootCategoriesWithAttributes();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.attributes")
    List<Category> findAllWithAttributes();
}
