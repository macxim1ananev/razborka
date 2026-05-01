package ru.razborka.marketplace.chat.realtime;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.razborka.marketplace.chat.realtime.dto.UserOnlineEventDto;
import ru.razborka.marketplace.listing.service.ChatService;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Component
public class ChatPresenceEventsListener {

    private final OnlineUsersRegistry onlineUsersRegistry;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatPresenceEventsListener(
            OnlineUsersRegistry onlineUsersRegistry,
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.onlineUsersRegistry = onlineUsersRegistry;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String sessionId = accessor.getSessionId();
        Long userId = parseUserId(principal);
        if (userId == null || sessionId == null) {
            return;
        }
        onlineUsersRegistry.onConnect(sessionId, userId);
        publishPresence(userId, true);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId == null) {
            return;
        }
        Long userId = onlineUsersRegistry.onDisconnect(sessionId);
        if (userId == null) {
            return;
        }
        boolean online = onlineUsersRegistry.isOnline(userId);
        if (!online) {
            publishPresence(userId, false);
        }
    }

    private void publishPresence(Long userId, boolean online) {
        List<Long> threadIds = chatService.threadIdsForUser(userId);
        UserOnlineEventDto event = new UserOnlineEventDto(null, userId, online, Instant.now());
        for (Long threadId : threadIds) {
            messagingTemplate.convertAndSend(
                    "/topic/chat.thread." + threadId + ".online",
                    new UserOnlineEventDto(threadId, event.userId(), event.online(), event.at())
            );
        }
    }

    private static Long parseUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return null;
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
