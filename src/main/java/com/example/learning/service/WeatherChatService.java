package com.example.learning.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class WeatherChatService {

    private final ChatClient weatherClient;

    public WeatherChatService(@Qualifier("weatherClient") ChatClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    public String chat(String message) {
        // 调用 AI，自动匹配已注册的 Tool（WeatherService.getWeather）
        return weatherClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
