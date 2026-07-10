package com.example.learning.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.TokenStream;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 天气助手（LangChain4j 版），绑定 weatherToolV0~V4 共5个变体。
 * 每个变体通过不同的 AiService 接口来测试。
 */
@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV0"})
interface WeatherAssistantV0 {
    @SystemMessage("""
            你是一个天气查询助手。
            当用户请求查询某个城市的天气时，无论城市名是否常见，都要调用 getWeather 工具。
            不要拒绝调用工具，也不要直接用自然语言回复天气查询。
            """)
    String chat(String userMessage);
    TokenStream chatStream(String userMessage);
}

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV1"})
interface WeatherAssistantV1 {
    @SystemMessage("""
            你是一个天气查询助手。
            当用户请求查询某个城市的天气时，无论城市名是否常见，都要调用 getWeather 工具。
            不要拒绝调用工具，也不要直接用自然语言回复天气查询。
            """)
    String chat(String userMessage);
    TokenStream chatStream(String userMessage);
}

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV2"})
interface WeatherAssistantV2 {
    @SystemMessage("""
            你是一个天气查询助手。
            当用户请求查询某个城市的天气时，无论城市名是否常见，都要调用 getWeather 工具。
            不要拒绝调用工具，也不要直接用自然语言回复天气查询。
            """)
    String chat(String userMessage);
    TokenStream chatStream(String userMessage);
}

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV3"})
interface WeatherAssistantV3 {
    @SystemMessage("""
            你是一个天气查询助手。
            当用户请求查询某个城市的天气时，无论城市名是否常见，都要调用 getWeather 工具。
            不要拒绝调用工具，也不要直接用自然语言回复天气查询。
            """)
    String chat(String userMessage);
    TokenStream chatStream(String userMessage);
}

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV4"})
interface WeatherAssistantV4 {
    @SystemMessage("""
            你是一个天气查询助手。
            当用户请求查询某个城市的天气时，无论城市名是否常见，都要调用 getWeather 工具。
            不要拒绝调用工具，也不要直接用自然语言回复天气查询。
            """)
    String chat(String userMessage);
    TokenStream chatStream(String userMessage);
}
