package org.blueseacode.websocket.handler;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionManager {

    /** 在线用户集合 (userId -> sessionId列表) */
    private final ConcurrentHashMap<Long, Set<String>> onlineUsers = new ConcurrentHashMap<>();

    /** 用户上线 */
    public void userOnline(Long userId, String sessionId) {
        onlineUsers.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    /** 用户下线 */
    public void userOffline(Long userId, String sessionId) {
        Set<String> sessions = onlineUsers.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                onlineUsers.remove(userId);
            }
        }
    }

    /** 判断用户是否在线 */
    public boolean isOnline(Long userId) {
        return onlineUsers.containsKey(userId) && !onlineUsers.get(userId).isEmpty();
    }

    /** 获取所有在线用户ID */
    public Set<Long> getAllOnlineUsers() {
        return onlineUsers.keySet();
    }
}
