package ru.razborka.marketplace.listing.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.listing.service.ChatService;
import ru.razborka.marketplace.listing.web.dto.ChatMessageDto;
import ru.razborka.marketplace.listing.web.dto.ChatThreadDto;
import ru.razborka.marketplace.listing.web.dto.SendChatMessageRequest;
import ru.razborka.marketplace.listing.web.dto.StartChatRequest;
import ru.razborka.marketplace.listing.web.dto.UnreadChatsDto;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/start")
    public ChatMessageDto startChat(@Valid @RequestBody StartChatRequest request) {
        return chatService.startChat(request);
    }

    @PostMapping("/{threadId}/messages")
    public ChatMessageDto sendMessage(
            @PathVariable Long threadId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        return chatService.sendMessage(threadId, request.message());
    }

    @GetMapping
    public List<ChatThreadDto> myThreads() {
        return chatService.myThreads();
    }

    @GetMapping("/{threadId}/messages")
    public List<ChatMessageDto> messages(
            @PathVariable Long threadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return chatService.threadMessages(threadId, page, size);
    }

    @GetMapping("/unread-count")
    public UnreadChatsDto unreadCount() {
        return new UnreadChatsDto(chatService.unreadCount());
    }
}
