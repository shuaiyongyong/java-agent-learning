package com.example.learning.config;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
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
}
