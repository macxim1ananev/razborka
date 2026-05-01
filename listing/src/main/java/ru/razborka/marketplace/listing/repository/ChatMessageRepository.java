package ru.razborka.marketplace.listing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.razborka.marketplace.listing.domain.ChatMessage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByThreadIdOrderByCreatedAtDescIdDesc(Long threadId, Pageable pageable);

    Optional<ChatMessage> findTopByThreadIdOrderByCreatedAtDescIdDesc(Long threadId);

    long countByThreadIdAndSenderIdNotAndReadAtIsNull(Long threadId, Long senderId);

    List<ChatMessage> findByThreadIdAndSenderIdNotAndReadAtIsNull(Long threadId, Long senderId);

    @Query("""
            SELECT COUNT(m)
            FROM ChatMessage m
            WHERE (m.thread.buyer.id = :userId OR m.thread.seller.id = :userId)
              AND m.sender.id <> :userId
              AND m.readAt IS NULL
            """)
    long countUnreadForUser(@Param("userId") Long userId);

    default void markAsRead(List<ChatMessage> messages) {
        Instant now = Instant.now();
        for (ChatMessage message : messages) {
            message.setReadAt(now);
        }
        saveAll(messages);
    }
}
