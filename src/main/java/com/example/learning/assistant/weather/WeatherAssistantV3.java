package com.example.learning.assistant.weather;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV3"})
public interface WeatherAssistantV3 {
    @SystemMessage("""
    你仅能通过 getWeather 工具来回答与天气相关的问题。
    如果用户未指定城市，你必须先反问用户提供城市名称，然后再调用工具。
    如果工具返回的数据中缺少某些字段（如湿度或风力），请如实说明数据缺失，不可编造补全。
    """)
    String chat(String userMessage);

    TokenStream chatStream(String userMessage);
}
