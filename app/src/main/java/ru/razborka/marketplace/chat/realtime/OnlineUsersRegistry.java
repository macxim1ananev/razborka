package ru.razborka.marketplace.chat.realtime;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUsersRegistry {

    private final Map<Long, Integer> onlineCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();

    public void onConnect(String sessionId, Long userId) {
        sessionToUser.put(sessionId, userId);
        onlineCounters.merge(userId, 1, Integer::sum);
    }

    public Long onDisconnect(String sessionId) {
        Long userId = sessionToUser.remove(sessionId);
        if (userId == null) {
            return null;
        }
        onlineCounters.computeIfPresent(userId, (k, v) -> v <= 1 ? null : v - 1);
        return userId;
    }

    public boolean isOnline(Long userId) {
        return onlineCounters.getOrDefault(userId, 0) > 0;
    }
}
