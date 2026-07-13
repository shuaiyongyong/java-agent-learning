package com.example.learning.assistant.weather;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV1"})
public interface WeatherAssistantV1 {
    @SystemMessage("""
    你是一名资深气象服务专员，精通全球气候数据。
    为了提供准确的天气预报，你拥有一个专用的天气查询终端（getWeather 工具）。
    你的工作流程是：先判断用户意图，若涉及天气，优先操作终端获取数据，再用通俗易懂的语言转述给用户。
    """)
    String chat(String userMessage);

    TokenStream chatStream(String userMessage);
}
