package ru.razborka.marketplace.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import ru.razborka.marketplace.auth.jwt.JwtService;
import ru.razborka.marketplace.common.exception.BusinessException;

import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor);
            Long userId = jwtService.parseAccessUserId(token)
                    .orElseThrow(() -> new BusinessException("UNAUTHORIZED", "Требуется валидный JWT для websocket"));
            var auth = new UsernamePasswordAuthenticationToken(
                    String.valueOf(userId),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            accessor.setUser(auth);
        }
        return message;
    }

    private static String resolveToken(StompHeaderAccessor accessor) {
        String auth = accessor.getFirstNativeHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();
            if (!token.isEmpty()) {
                return token;
            }
        }
        String token = accessor.getFirstNativeHeader("access_token");
        if (token != null && !token.isBlank()) {
            return token.trim();
        }
        throw new BusinessException("UNAUTHORIZED", "Отсутствует JWT для websocket");
    }
}
