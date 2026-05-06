package org.blueseacode.ai.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.blueseacode.ai.chat.MockChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * AI 模型配置
 * 支持火山引擎豆包大模型（OpenAI 兼容模式）和开发 Mock 模式
 *
 * 生产配置示例（application.yaml）：
 *   volc-engine:
 *     api-key: xxxxxx
 *     model: ep-xxxxxx-xxxxxx    # 推理接入点ID
 *     base-url: https://ark.cn-beijing.volces.com/api/v3/
 */
@Configuration
@Slf4j
public class AIConfig {

    /**
     * 开发模式：Mock ChatModel（内置回复模板）
     * 当配置 volc-engine.api-key=mock 或未配置时生效
     */
    @Bean
    @ConditionalOnProperty(name = "volc-engine.api-key", havingValue = "mock", matchIfMissing = true)
    public ChatModel mockChatModel() {
        log.info("[AIConfig] 使用 Mock AI 模式（开发阶段）");
        return new MockChatModel();
    }

    /**
     * 生产模式：火山引擎豆包大模型（OpenAI 兼容 API）
     * 通过 LangChain4j 的 OpenAiChatModel 对接
     * 当配置了真实 api-key（非 mock）时生效
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "volc-engine.api-key", matchIfMissing = false)
    public ChatModel volcEngineChatModel(
            @Value("${volc-engine.api-key}") String apiKey,
            @Value("${volc-engine.base-url:https://ark.cn-beijing.volces.com/api/v3/}") String baseUrl,
            @Value("${volc-engine.model:doubao-pro-32k}") String modelName,
            @Value("${volc-engine.max-tokens:2048}") int maxTokens,
            @Value("${volc-engine.temperature:0.7}") double temperature) {

        log.info("[AIConfig] 初始化火山引擎豆包大模型, model={}, baseUrl={}", modelName, baseUrl);

        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
