package ru.razborka.marketplace.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.razborka.marketplace.listing.domain.ChatThread;

import java.util.List;
import java.util.Optional;

public interface ChatThreadRepository extends JpaRepository<ChatThread, Long> {

    Optional<ChatThread> findByListingIdAndBuyerId(Long listingId, Long buyerId);

    @Query("""
            SELECT t
            FROM ChatThread t
            JOIN FETCH t.listing l
            JOIN FETCH t.buyer b
            JOIN FETCH t.seller s
            WHERE t.id = :threadId
            """)
    Optional<ChatThread> findDetailedById(@Param("threadId") Long threadId);

    @Query("""
            SELECT t
            FROM ChatThread t
            WHERE t.buyer.id = :userId OR t.seller.id = :userId
            ORDER BY COALESCE(t.lastMessageAt, t.createdAt) DESC
            """)
    List<ChatThread> findAllForUser(@Param("userId") Long userId);

    @Query("""
            SELECT t.id
            FROM ChatThread t
            WHERE t.buyer.id = :userId OR t.seller.id = :userId
            """)
    List<Long> findThreadIdsForUser(@Param("userId") Long userId);
}
