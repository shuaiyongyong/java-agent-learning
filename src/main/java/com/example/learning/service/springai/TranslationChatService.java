package com.example.learning.service.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TranslationChatService {

    private final ChatClient translatorClient;

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
