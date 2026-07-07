package com.example.learning.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.TokenStream;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 基于 LangChain4j 的客服助手。
 * <p>
 * 通过 {@link AiService} 声明式生成实现，绑定到 Spring 容器中名为 {@code langchainOllamaChatModel} 的
 * 模型 Bean（在 {@code LangChain4jOllamaConfig} 中根据
 * {@code langchain4j.ollama.chat-model.*} 配置手动创建）。
 * 流式接口绑定到 {@code langchainOllamaStreamingChatModel}。
 */
@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherService"})
public interface WeatherAssistant {

    @SystemMessage("""
            你是一个天气查询助手。
            当用户请求查询某个城市的天气时，无论城市名是否常见。不要拒绝调用工具，也不要直接用自然语言回复天气查询
            """)
    String chat(String userMessage);

    /**
     * 流式聊天接口，返回 TokenStream，可通过 onPartialResponse 逐块接收回复。
     */
    TokenStream chatStream(String userMessage);
}
