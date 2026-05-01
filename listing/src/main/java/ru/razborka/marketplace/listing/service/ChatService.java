package ru.razborka.marketplace.listing.service;

import ru.razborka.marketplace.listing.web.dto.ChatMessageDto;
import ru.razborka.marketplace.listing.web.dto.ChatThreadMembersDto;
import ru.razborka.marketplace.listing.web.dto.ChatThreadDto;
import ru.razborka.marketplace.listing.web.dto.StartChatRequest;

import java.util.List;

public interface ChatService {
    ChatMessageDto startChat(StartChatRequest request);

    ChatMessageDto sendMessage(Long threadId, String body);

    List<ChatThreadDto> myThreads();

    List<ChatMessageDto> threadMessages(Long threadId, int page, int size);

    long unreadCount();

    ChatThreadMembersDto threadMembers(Long threadId, Long currentUserId);

    List<Long> threadIdsForUser(Long userId);
}
