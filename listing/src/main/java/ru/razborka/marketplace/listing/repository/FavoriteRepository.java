package ru.razborka.marketplace.listing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.razborka.marketplace.listing.domain.Favorite;
import ru.razborka.marketplace.listing.domain.Favorite.FavoriteId;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    @EntityGraph(attributePaths = {"listing", "listing.seller", "listing.photos"})
    Page<Favorite> findByIdUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByIdUserIdAndIdListingId(Long userId, Long listingId);
}
