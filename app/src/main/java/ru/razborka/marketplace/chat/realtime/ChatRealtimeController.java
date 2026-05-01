package ru.razborka.marketplace.chat.realtime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import ru.razborka.marketplace.common.exception.BusinessException;
import ru.razborka.marketplace.listing.service.ChatService;
import ru.razborka.marketplace.listing.web.dto.ChatThreadMembersDto;
import ru.razborka.marketplace.chat.realtime.dto.ThreadOnlineSnapshotDto;
import ru.razborka.marketplace.chat.realtime.dto.TypingSignalInDto;
import ru.razborka.marketplace.chat.realtime.dto.TypingSignalOutDto;
import ru.razborka.marketplace.chat.realtime.dto.UserOnlineStateDto;

import java.security.Principal;
import java.time.Instant;

@Controller
public class ChatRealtimeController {

    private final ChatService chatService;
    private final OnlineUsersRegistry onlineUsersRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRealtimeController(
            ChatService chatService,
            OnlineUsersRegistry onlineUsersRegistry,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatService = chatService;
        this.onlineUsersRegistry = onlineUsersRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.thread.{threadId}.typing")
    public void typing(
            @DestinationVariable Long threadId,
            @Payload TypingSignalInDto payload,
            Principal principal
    ) {
        Long userId = currentUserId(principal);
        chatService.threadMembers(threadId, userId);
        boolean typing = payload != null && Boolean.TRUE.equals(payload.typing());
        messagingTemplate.convertAndSend(
                "/topic/chat.thread." + threadId + ".typing",
                new TypingSignalOutDto(threadId, userId, typing, Instant.now())
        );
    }

    @SubscribeMapping("/chat.thread.{threadId}.online")
    @SendTo("/topic/chat.thread.{threadId}.online")
    public ThreadOnlineSnapshotDto onlineSnapshot(
            @DestinationVariable Long threadId,
            Principal principal
    ) {
        Long userId = currentUserId(principal);
        ChatThreadMembersDto members = chatService.threadMembers(threadId, userId);
        return new ThreadOnlineSnapshotDto(
                threadId,
                new UserOnlineStateDto(members.buyerId(), onlineUsersRegistry.isOnline(members.buyerId())),
                new UserOnlineStateDto(members.sellerId(), onlineUsersRegistry.isOnline(members.sellerId()))
        );
    }

    private static Long currentUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new BusinessException("UNAUTHORIZED", "Нет websocket пользователя");
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException ex) {
            throw new BusinessException("UNAUTHORIZED", "Невалидный websocket пользователь");
        }
    }
}
