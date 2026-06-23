package com.example.learning.controller;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OllamaController {

    private final OllamaChatModel chatModel;

    public OllamaController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/chat/model")
    public String chatWithModel(@RequestParam String message) {

        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        OllamaOptions.builder()
                                .temperature(0.8)
                                .build()
                ));
        return response.getResult().getOutput().toString();
    }
}

