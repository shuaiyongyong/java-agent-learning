package com.example.learning.assistant.weather;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV2"})
public interface WeatherAssistantV2 {
    @SystemMessage("""
    你是一个智能出行规划助手。当用户在规划行程、询问穿衣建议或户外活动可行性时，
    你需要主动调用 getWeather 工具来获取目的地天气，并以此为依据提供个性化建议。
    请务必基于工具回传的真实天气数据给出结论，不要主观臆断。
    """)
    String chat(String userMessage);

    TokenStream chatStream(String userMessage);
}
