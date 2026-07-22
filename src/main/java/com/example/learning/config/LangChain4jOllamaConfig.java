package com.example.learning.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
 * <p>
 * 多轮对话记忆通过 {@link JdbcChatMemoryStore} 持久化到 MySQL 数据库，
 * 应用重启后对话历史可自动恢复。
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
     * 基于 JDBC（MySQL 数据库）的 ChatMemoryStore Bean。
     * <p>
     * 替代原来的 ConcurrentHashMap 实现，使对话消息持久化到磁盘。
     * 应用重启后，MessageWindowChatMemory 会从数据库加载历史消息，
     * 实现跨会话的记忆恢复。
     * <p>
     * 注意：此处返回 JdbcChatMemoryStore 实例，Spring 会自动注入已标注 @Component 的同类 Bean。
     * 为保证 Bean 名称一致，直接复用 JdbcChatMemoryStore 的 Spring Bean。
     */
    @Bean
    public ChatMemoryStore chatMemoryStore(JdbcChatMemoryStore jdbcChatMemoryStore) {
        return jdbcChatMemoryStore;
    }

    /**
     * ChatMemoryProvider Bean，供 @AiService 通过 chatMemoryProvider = "chatMemoryProvider" 引用。
     * <p>
     * 为每个唯一的 memoryId 创建独立的 MessageWindowChatMemory，
     * 最多保留 10 条消息（系统消息除外），实现滑动窗口记忆。
     * 底层存储通过 JdbcChatMemoryStore 持久化到 MySQL 数据库。
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}
