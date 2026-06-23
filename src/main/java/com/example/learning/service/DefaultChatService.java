package com.example.learning.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class DefaultChatService {

    private final ChatClient defaultClient;

    // 构造器注入
    public DefaultChatService(@Qualifier("defaultClient") ChatClient defaultClient) {
        this.defaultClient = defaultClient;
    }

    public String chat(String message) {
        return defaultClient
                .prompt()
                .user(message)
                .call()
                .content();
    }
}
