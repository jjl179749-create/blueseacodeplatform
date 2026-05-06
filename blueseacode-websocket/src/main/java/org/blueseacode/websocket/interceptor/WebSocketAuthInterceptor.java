package org.blueseacode.websocket.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blueseacode.security.jwt.JwtTokenProvider;
import org.blueseacode.websocket.handler.UserSessionManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionManager sessionManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtTokenProvider.validateToken(token) && !jwtTokenProvider.isRefreshToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    List<String> roles = jwtTokenProvider.getRolesFromToken(token);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    accessor.setUser(auth);

                    // 记录在线状态
                    String sessionId = accessor.getSessionId();
                    if (sessionId != null) {
                        sessionManager.userOnline(userId, sessionId);
                        log.debug("WebSocket 用户上线: userId={}, sessionId={}", userId, sessionId);
                    }
                } else {
                    log.warn("WebSocket 连接 Token 无效");
                }
            }
        }

        // 断开连接时移除在线状态
        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null && accessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
                Long userId = (Long) auth.getPrincipal();
                String sessionId = accessor.getSessionId();
                if (userId != null && sessionId != null) {
                    sessionManager.userOffline(userId, sessionId);
                    log.debug("WebSocket 用户离线: userId={}, sessionId={}", userId, sessionId);
                }
            }
        }

        return message;
    }
}
