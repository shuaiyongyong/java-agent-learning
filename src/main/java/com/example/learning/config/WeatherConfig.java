package com.example.learning.config;

import com.example.learning.tool.weather.WeatherToolV0;
import com.example.learning.tool.weather.WeatherToolV1;
import com.example.learning.tool.weather.WeatherToolV2;
import com.example.learning.tool.weather.WeatherToolV3;
import com.example.learning.tool.weather.WeatherToolV4;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 天气工具描述对比实验配置。
 * <p>
 * 为5种变体各创建一个独立的 ChatClient，每个绑定一个只含单一 @Tool 方法的 Service，
 * 确保各变体之间唯一的变量是 @Tool 的 description 文本。
 */
@Configuration
public class WeatherConfig {

    private static final String SYSTEM_PROMPT = """
            你是一个智能助手，可以回答用户的各种问题。
            如果你知道答案，直接回答；如果需要实时数据（如天气），请使用可用的工具。
            始终使用简体中文回复。
            """;

    @Bean("weatherToolV0")
    public WeatherToolV0 weatherToolV0() {
        return new WeatherToolV0();
    }

    @Bean("weatherToolV1")
    public WeatherToolV1 weatherToolV1() {
        return new WeatherToolV1();
    }

    @Bean("weatherToolV2")
    public WeatherToolV2 weatherToolV2() {
        return new WeatherToolV2();
    }

    @Bean("weatherToolV3")
    public WeatherToolV3 weatherToolV3() {
        return new WeatherToolV3();
    }

    @Bean("weatherToolV4")
    public WeatherToolV4 weatherToolV4() {
        return new WeatherToolV4();
    }

    @Bean("testChatClientV0")
    public ChatClient testChatClientV0(OllamaChatModel chatModel, WeatherToolV0 service) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(ToolCallbacks.from(service))
                .build();
    }

    @Bean("testChatClientV1")
    public ChatClient testChatClientV1(OllamaChatModel chatModel, WeatherToolV1 service) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(ToolCallbacks.from(service))
                .build();
    }

    @Bean("testChatClientV2")
    public ChatClient testChatClientV2(OllamaChatModel chatModel, WeatherToolV2 service) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(ToolCallbacks.from(service))
                .build();
    }

    @Bean("testChatClientV3")
    public ChatClient testChatClientV3(OllamaChatModel chatModel, WeatherToolV3 service) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(ToolCallbacks.from(service))
                .build();
    }

    @Bean("testChatClientV4")
    public ChatClient testChatClientV4(OllamaChatModel chatModel, WeatherToolV4 service) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(ToolCallbacks.from(service))
                .build();
    }
}
