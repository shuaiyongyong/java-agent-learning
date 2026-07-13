package com.example.learning.assistant.weather;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"weatherToolV4"})
public interface WeatherAssistantV4 {
    @SystemMessage("""
    请按以下步骤处理用户的天气请求：
    步骤 1：从用户输入中提取目标城市名称。
    步骤 2：调用 getWeather 工具，将提取到的城市作为参数传入。
    步骤 3：解析工具返回的 JSON 数据，提取温度、天气状况和风力信息。
    步骤 4：将上述数据组织成一段自然流畅的中文回复，语气亲和友好。
    """)
    String chat(String userMessage);

    TokenStream chatStream(String userMessage);
}
