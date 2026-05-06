package org.blueseacode.ai.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blueseacode.dao.entity.ChatConversation;
import org.blueseacode.dao.mapper.ChatConversationMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 多轮对话记忆管理器
 * - 每个 sessionId 对应一个 ChatMemory 实例
 * - 首次访问时从 MySQL 加载历史对话作为 Redis 预热
 * - 通过 MessageWindowChatMemory + RedisChatMemoryStore 实现持久化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMemoryManager {

    private final ChatMemoryStore memoryStore;
    private final ChatConversationMapper conversationMapper;

    /** 本地缓存 ChatMemory 实例，避免重复创建 */
    private final ConcurrentMap<String, ChatMemory> memoryCache = new ConcurrentHashMap<>();

    private static final int MAX_MESSAGES = 20; // 最多保留 20 条消息（10 轮对话）

    /**
     * 获取指定会话的多轮对话记忆
     * 首次访问时自动从 MySQL 加载历史记录预热 Redis
     */
    public ChatMemory getChatMemory(String sessionId) {
        return memoryCache.computeIfAbsent(sessionId, this::createChatMemory);
    }

    /**
     * 创建 MessageWindowChatMemory 实例
     * 如果 Redis 中没有该会话数据，从 MySQL 加载并写入 Redis
     */
    private ChatMemory createChatMemory(String sessionId) {
        // 先从 MySQL 加载历史对话预热 Redis
        warmUpFromMysql(sessionId);

        return MessageWindowChatMemory.builder()
                .id(sessionId)
                .maxMessages(MAX_MESSAGES)
                .chatMemoryStore(memoryStore)
                .build();
    }

    /**
     * 从 MySQL 加载历史对话到 Redis
     */
    private void warmUpFromMysql(String sessionId) {
        try {
            List<ChatConversation> history = conversationMapper.selectList(
                    new LambdaQueryWrapper<ChatConversation>()
                            .eq(ChatConversation::getSessionId, sessionId)
                            .orderByAsc(ChatConversation::getCreateTime));

            if (!history.isEmpty()) {
                List<ChatMessage> messages = new ArrayList<>();
                for (ChatConversation record : history) {
                    messages.add(UserMessage.from(record.getQuestion()));
                    messages.add(AiMessage.from(record.getAnswer()));
                }
                // 写入 Redis 作为预热（MessageWindowChatMemory 初始化时会从 store 加载）
                memoryStore.updateMessages(sessionId, messages);
                log.debug("[ChatMemory] MySQL历史已预热到Redis, sessionId={}, count={}",
                        sessionId, history.size());
            }
        } catch (Exception e) {
            log.warn("[ChatMemory] MySQL预热失败, sessionId={}, err={}", sessionId, e.getMessage());
        }
    }

    /**
     * 清除指定会话的记忆
     */
    public void removeChatMemory(String sessionId) {
        ChatMemory removed = memoryCache.remove(sessionId);
        if (removed != null) {
            removed.clear();
        }
        memoryStore.deleteMessages(sessionId);
    }

    @PreDestroy
    public void clearAll() {
        memoryCache.clear();
    }
}
