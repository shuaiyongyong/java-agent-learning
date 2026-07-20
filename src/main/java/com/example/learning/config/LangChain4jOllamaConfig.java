package com.example.learning.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LangChain4j 的 Ollama 模型配置。
 * <p>
 * 项目中同时引入了 Spring AI 与 LangChain4j 两套 Ollama 集成，二者的自动配置都会注册名为
 * {@code ollamaChatModel} 的 Bean（类型不同），导致启动时 Bean 名称冲突。
 * <p>
 * 解决方案：在 application.properties 中排除 LangChain4j 的 Ollama 自动配置
 * （{@code dev.langchain4j.ollama.spring.AutoConfig}），保留 Spring AI 的 {@code ollamaChatModel}
 * （被 ChatClient 按类型注入），并在此手动声明 LangChain4j 的模型 Bean，使用独立名称
 * {@code langchainOllamaChatModel} 供 {@code @AiService} 按名称引用。
 */
@Configuration
public class LangChain4jOllamaConfig {

    @Bean
    public OllamaChatModel langchainOllamaChatModel(
            @Value("${langchain4j.ollama.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.ollama.chat-model.model-name}") String modelName) {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }

    /**
     * LangChain4j 流式聊天模型 Bean，供 @AiService 的 streamingChatModel 属性引用。
     */
    @Bean
    public OllamaStreamingChatModel langchainOllamaStreamingChatModel(
            @Value("${langchain4j.ollama.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.ollama.chat-model.model-name}") String modelName) {
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }

    // ==================== 多轮对话记忆 ====================

    /**
     * 基于 ConcurrentHashMap 的 ChatMemoryStore 实现，支持多会话隔离。
     * <p>
     * 默认的 {@code SingleSlotChatMemoryStore} 只能保存一个会话的消息，
     * 无法区分不同用户的对话。此实现使用 ConcurrentHashMap 以 memoryId 为 key
     * 存储各自的聊天消息列表，从而实现多轮对话记忆。
     */
    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new ChatMemoryStore() {
            private final Map<Object, List<dev.langchain4j.data.message.ChatMessage>> store = new ConcurrentHashMap<>();

            @Override
            public List<dev.langchain4j.data.message.ChatMessage> getMessages(Object memoryId) {
                return store.getOrDefault(memoryId, List.of());
            }

            @Override
            public void updateMessages(Object memoryId, List<dev.langchain4j.data.message.ChatMessage> messages) {
                store.put(memoryId, List.copyOf(messages));
            }

            @Override
            public void deleteMessages(Object memoryId) {
                store.remove(memoryId);
            }
        };
    }

    /**
     * ChatMemoryProvider Bean，供 @AiService 通过 chatMemoryProvider = "chatMemoryProvider" 引用。
     * <p>
     * 为每个唯一的 memoryId 创建独立的 MessageWindowChatMemory，
     * 最多保留 20 条消息（系统消息除外），实现滑动窗口记忆。
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}
