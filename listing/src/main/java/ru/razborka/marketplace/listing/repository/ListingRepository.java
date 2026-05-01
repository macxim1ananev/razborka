package ru.razborka.marketplace.listing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.razborka.marketplace.listing.domain.Listing;
import ru.razborka.marketplace.listing.domain.ListingCompatibility;
import ru.razborka.marketplace.listing.domain.ListingStatus;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @EntityGraph(attributePaths = {"seller", "category", "photos"})
    Page<Listing> findByStatusOrderByCreatedAtDesc(ListingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "category", "photos"})
    @Query("""
            SELECT DISTINCT l
            FROM Listing l
            WHERE l.status = ru.razborka.marketplace.listing.domain.ListingStatus.active
              AND (
                    NOT EXISTS (
                        SELECT 1 FROM UserCar ucNoActive
                        WHERE ucNoActive.user.id = :userId AND ucNoActive.active = true
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM ListingCompatibility c, UserCar uc
                        WHERE c.listing = l
                          AND uc.user.id = :userId
                          AND uc.active = true
                          AND lower(c.brand) = lower(uc.brand)
                          AND lower(c.model) = lower(uc.model)
                          AND (c.generation IS NULL OR uc.generation IS NULL OR lower(c.generation) = lower(uc.generation))
                          AND (uc.year IS NULL OR ((c.yearFrom IS NULL OR c.yearFrom <= uc.year) AND (c.yearTo IS NULL OR c.yearTo >= uc.year)))
                          AND (uc.engineVolume IS NULL OR c.engineVolume IS NULL OR abs(c.engineVolume - uc.engineVolume) <= 0.11)
                    )
                )
            ORDER BY l.createdAt DESC
            """)
    Page<Listing> findActiveMatchingActiveUserCar(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT DISTINCT l FROM Listing l
            JOIN FETCH l.seller s
            JOIN FETCH l.category c
            LEFT JOIN FETCH l.photos
            WHERE l.id = :id
            """)
    Optional<Listing> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"seller", "category", "photos"})
    Page<Listing> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, ListingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "category", "photos"})
    Page<Listing> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "category", "photos"})
    java.util.List<Listing> findTop4BySellerIdAndIdNotAndStatusOrderByCreatedAtDesc(
            Long sellerId,
            Long excludeId,
            ListingStatus status
    );

    @Query("SELECT l.id FROM Listing l WHERE l.status = :status ORDER BY l.createdAt DESC")
    List<Long> findIdsByStatus(@Param("status") ListingStatus status);
}
