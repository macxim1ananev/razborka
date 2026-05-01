package ru.razborka.marketplace.listing.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razborka.marketplace.common.exception.BusinessException;
import ru.razborka.marketplace.common.exception.ForbiddenException;
import ru.razborka.marketplace.common.exception.NotFoundException;
import ru.razborka.marketplace.common.security.SecurityUtils;
import ru.razborka.marketplace.listing.domain.ChatMessage;
import ru.razborka.marketplace.listing.domain.ChatThread;
import ru.razborka.marketplace.listing.domain.Listing;
import ru.razborka.marketplace.listing.domain.ListingStatus;
import ru.razborka.marketplace.listing.repository.ChatMessageRepository;
import ru.razborka.marketplace.listing.repository.ChatThreadRepository;
import ru.razborka.marketplace.listing.repository.ListingRepository;
import ru.razborka.marketplace.listing.web.dto.ChatMessageDto;
import ru.razborka.marketplace.listing.web.dto.ChatThreadMembersDto;
import ru.razborka.marketplace.listing.web.dto.ChatThreadDto;
import ru.razborka.marketplace.listing.web.dto.StartChatRequest;
import ru.razborka.marketplace.user.domain.User;
import ru.razborka.marketplace.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

    public ChatServiceImpl(
            ChatThreadRepository chatThreadRepository,
            ChatMessageRepository chatMessageRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider
    ) {
        this.chatThreadRepository = chatThreadRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.messagingTemplateProvider = messagingTemplateProvider;
    }

    @Override
    @Transactional
    public ChatMessageDto startChat(StartChatRequest request) {
        long currentUserId = SecurityUtils.requireUserId();
        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
        if (listing.getStatus() != ListingStatus.active) {
            throw new BusinessException("CHAT", "Чат доступен только для активных объявлений");
        }
        if (listing.getSeller().getId().equals(currentUserId)) {
            throw new BusinessException("CHAT", "Нельзя начать чат со своим объявлением");
        }
        ChatThread thread = chatThreadRepository.findByListingIdAndBuyerId(request.listingId(), currentUserId)
                .orElseGet(() -> createThread(listing, currentUserId));
        return persistMessage(thread, currentUserId, request.message());
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessage(Long threadId, String body) {
        long currentUserId = SecurityUtils.requireUserId();
        ChatThread thread = requireThreadWithAccess(threadId, currentUserId);
        return persistMessage(thread, currentUserId, body);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatThreadDto> myThreads() {
        long currentUserId = SecurityUtils.requireUserId();
        List<ChatThread> threads = chatThreadRepository.findAllForUser(currentUserId);
        return threads.stream()
                .map(t -> toThreadDto(t, currentUserId))
                .toList();
    }

    @Override
    @Transactional
    public List<ChatMessageDto> threadMessages(Long threadId, int page, int size) {
        long currentUserId = SecurityUtils.requireUserId();
        ChatThread thread = requireThreadWithAccess(threadId, currentUserId);
        var messages = chatMessageRepository.findByThreadIdOrderByCreatedAtDescIdDesc(
                        threadId,
                        PageRequest.of(page, Math.max(1, Math.min(size, 100)))
                )
                .getContent();
        List<ChatMessage> unread = chatMessageRepository.findByThreadIdAndSenderIdNotAndReadAtIsNull(threadId, currentUserId);
        if (!unread.isEmpty()) {
            chatMessageRepository.markAsRead(unread);
        }
        return messages.stream().map(this::toMessageDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount() {
        long currentUserId = SecurityUtils.requireUserId();
        return chatMessageRepository.countUnreadForUser(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatThreadMembersDto threadMembers(Long threadId, Long currentUserId) {
        ChatThread thread = requireThreadWithAccess(threadId, currentUserId);
        return new ChatThreadMembersDto(thread.getId(), thread.getBuyer().getId(), thread.getSeller().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> threadIdsForUser(Long userId) {
        return chatThreadRepository.findThreadIdsForUser(userId);
    }

    private ChatThread createThread(Listing listing, long buyerId) {
        ChatThread thread = new ChatThread();
        thread.setListing(listing);
        thread.setSeller(listing.getSeller());
        User buyer = userRepository.getReferenceById(buyerId);
        thread.setBuyer(buyer);
        thread.setCreatedAt(Instant.now());
        thread.setUpdatedAt(Instant.now());
        return chatThreadRepository.save(thread);
    }

    private ChatThread requireThreadWithAccess(Long threadId, long currentUserId) {
        ChatThread thread = chatThreadRepository.findDetailedById(threadId)
                .orElseThrow(() -> new NotFoundException("Чат не найден"));
        boolean member = thread.getBuyer().getId().equals(currentUserId) || thread.getSeller().getId().equals(currentUserId);
        if (!member) {
            throw new ForbiddenException("Нет доступа к этому чату");
        }
        return thread;
    }

    private ChatMessageDto persistMessage(ChatThread thread, long currentUserId, String body) {
        String normalized = body == null ? "" : body.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("CHAT", "Сообщение не может быть пустым");
        }
        if (normalized.length() > 4000) {
            throw new BusinessException("CHAT", "Сообщение слишком длинное");
        }
        ChatMessage message = new ChatMessage();
        message.setThread(thread);
        message.setSender(userRepository.getReferenceById(currentUserId));
        message.setBody(normalized);
        message.setCreatedAt(Instant.now());
        message = chatMessageRepository.save(message);
        thread.setUpdatedAt(Instant.now());
        thread.setLastMessageAt(message.getCreatedAt());
        chatThreadRepository.save(thread);
        ChatMessageDto dto = toMessageDto(message);
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/chat.thread." + thread.getId() + ".messages", dto);
        }
        return dto;
    }

    private ChatThreadDto toThreadDto(ChatThread thread, long currentUserId) {
        boolean amBuyer = thread.getBuyer().getId().equals(currentUserId);
        User other = amBuyer ? thread.getSeller() : thread.getBuyer();
        var last = chatMessageRepository.findTopByThreadIdOrderByCreatedAtDescIdDesc(thread.getId());
        long unread = chatMessageRepository.countByThreadIdAndSenderIdNotAndReadAtIsNull(thread.getId(), currentUserId);
        return new ChatThreadDto(
                thread.getId(),
                thread.getListing().getId(),
                thread.getListing().getTitle(),
                other.getId(),
                other.getFirstName() == null ? "" : other.getFirstName(),
                other.getUsername() == null ? "" : other.getUsername(),
                last.map(ChatMessage::getBody).orElse(""),
                last.map(ChatMessage::getCreatedAt).orElse(thread.getCreatedAt()),
                unread
        );
    }

    private ChatMessageDto toMessageDto(ChatMessage message) {
        User sender = message.getSender();
        return new ChatMessageDto(
                message.getId(),
                message.getThread().getId(),
                sender.getId(),
                sender.getFirstName() == null ? "" : sender.getFirstName(),
                sender.getUsername() == null ? "" : sender.getUsername(),
                message.getBody(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }
}
