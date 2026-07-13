package com.example.learning.assistant.weather;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV0"})
public interface WeatherAssistantV0 {
    @SystemMessage("""
    你是一个天气信息助手。当用户询问任何城市的天气时，你必须立即调用 getWeather 工具获取实时数据。
    严禁使用你自身的知识库回答天气问题，必须完全依赖工具返回的结果。
    如果工具调用失败，需如实告知用户，并建议其稍后重试。
    """)
    String chat(String userMessage);

    TokenStream chatStream(String userMessage);
}
