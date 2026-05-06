package org.blueseacode.ai.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blueseacode.ai.faq.FaqService;
import org.blueseacode.ai.faq.KnowledgeBaseService;
import org.blueseacode.ai.model.ChatRequest;
import org.blueseacode.ai.model.ChatResponse;
import org.blueseacode.dao.entity.ChatConversation;
import org.blueseacode.dao.entity.ChatFaq;
import org.blueseacode.dao.entity.ChatKnowledgeBase;
import org.blueseacode.dao.mapper.ChatConversationMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI 对话服务
 * 多级应答策略：FAQ匹配 → 知识库检索(RAG) → LLM直接调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

    private final ChatModel chatModel;
    private final FaqService faqService;
    private final KnowledgeBaseService kbService;
    private final ChatConversationMapper conversationMapper;
    private final ChatMemoryManager chatMemoryManager;

    private static final String SYSTEM_PROMPT = "你是蓝海编程平台的智能客服小蓝。你热情、专业，帮助用户解决平台相关的问题。" +
            "请根据上下文和历史记录回答用户问题。如果用户的问题超出你的知识范围，请如实告知，" +
            "并建议用户创建工单转人工客服处理。";

    /**
     * 处理用户消息（三级应答策略 + 多轮对话记忆）
     * 策略1：FAQ 精确/模糊匹配
     * 策略2：知识库检索 + LLM 增强回答 (RAG)
     * 策略3：直接调用 LLM（带多轮上下文）
     */
    public ChatResponse processMessage(ChatRequest request) {
        String question = request.getQuestion();
        Long userId = request.getUserId();
        String sessionId = request.getSessionId();

        // 确保会话ID存在
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // === 获取多轮对话记忆（所有策略共用） ===
        ChatMemory chatMemory = chatMemoryManager.getChatMemory(sessionId);

        // === 策略1: FAQ 匹配（也记录到多轮记忆中） ===
        List<ChatFaq> faqs = faqService.matchByKeyword(question);
        if (!faqs.isEmpty()) {
            ChatFaq bestMatch = faqs.get(0);
            faqService.incrementHit(bestMatch.getId());
            chatMemory.add(UserMessage.from(question));
            chatMemory.add(AiMessage.from(bestMatch.getAnswer()));
            saveHistory(userId, sessionId, question, bestMatch.getAnswer(), "FAQ");
            log.info("[AIChat] FAQ命中: userId={}, question={}", userId, question);
            return ChatResponse.faq(bestMatch.getAnswer(), sessionId);
        }

        // === 策略2: 知识库检索 → LLM 增强回答 (RAG + 多轮上下文) ===
        List<ChatKnowledgeBase> kbResults = kbService.search(question);
        if (!kbResults.isEmpty()) {
            kbResults.forEach(kb -> kbService.incrementHit(kb.getId()));

            String context = kbResults.stream()
                    .map(ChatKnowledgeBase::getContent)
                    .collect(Collectors.joining("\n---\n"));

            // 将用户问题加入记忆
            chatMemory.add(UserMessage.from(question));

            // 构建 RAG 系统提示词（不保存在记忆中，仅在本次请求使用）
            String ragPrompt = "请根据以下知识库内容回答用户问题。" +
                    "如果知识库内容不足以回答，请如实告知用户，并引导用户创建工单。\n\n" +
                    "【知识库内容】\n" + context;

            // 组装完整消息列表：RAG 系统提示 + 历史对话
            List<dev.langchain4j.data.message.ChatMessage> allMessages = new ArrayList<>();
            allMessages.add(SystemMessage.from(ragPrompt));
            allMessages.addAll(chatMemory.messages());

            // 带完整上下文的 LLM 调用
            dev.langchain4j.model.chat.response.ChatResponse lcResponse = chatModel.chat(
                    dev.langchain4j.model.chat.request.ChatRequest.builder()
                            .messages(allMessages)
                            .build());
            String answer = lcResponse.aiMessage().text();

            // 将 AI 回答加入记忆
            chatMemory.add(AiMessage.from(answer));
            saveHistory(userId, sessionId, question, answer, "KNOWLEDGE");
            log.info("[AIChat] 知识库+RAG: userId={}, question={}", userId, question);
            return ChatResponse.knowledge(answer, sessionId);
        }

        // === 策略3: 直接调用 LLM（带多轮上下文） ===
        chatMemory.add(UserMessage.from(question));

        // 组装完整消息列表：系统提示 + 历史对话
        List<dev.langchain4j.data.message.ChatMessage> allMessages = new ArrayList<>();
        allMessages.add(SystemMessage.from(SYSTEM_PROMPT));
        allMessages.addAll(chatMemory.messages());

        dev.langchain4j.model.chat.response.ChatResponse lcResponse = chatModel.chat(
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(allMessages)
                        .build());
        String answer = lcResponse.aiMessage().text();

        chatMemory.add(AiMessage.from(answer));
        saveHistory(userId, sessionId, question, answer, "AI");
        log.info("[AIChat] AI回答(带上下文): userId={}, question={}", userId, question);
        return ChatResponse.ai(answer, sessionId);
    }

    /** 保存对话历史到 MySQL */
    private void saveHistory(Long userId, String sessionId, String question,
                             String answer, String answerType) {
        ChatConversation record = new ChatConversation();
        record.setUserId(userId);
        record.setSessionId(sessionId);
        record.setQuestion(question);
        record.setAnswer(answer);
        record.setAnswerType(answerType);
        record.setAiModel("VOLC_ENGINE");
        conversationMapper.insert(record);
    }

    /** 获取历史对话 */
    public List<ChatConversation> getHistory(Long userId, String sessionId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<ChatConversation>()
                        .eq(ChatConversation::getUserId, userId)
                        .eq(sessionId != null && !sessionId.isEmpty(),
                                ChatConversation::getSessionId, sessionId)
                        .orderByAsc(ChatConversation::getCreateTime));
    }
}
