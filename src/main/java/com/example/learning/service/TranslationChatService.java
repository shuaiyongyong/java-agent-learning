package com.example.learning.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class TranslationChatService {

    private final ChatClient translatorClient;

    // 构造器注入
    public TranslationChatService(@Qualifier("translatorClient") ChatClient translatorClient) {
        this.translatorClient = translatorClient;
    }

    public String chat(String message) {
        return translatorClient
                .prompt()
                .user(message)
                .call()
                .content();
    }
}
