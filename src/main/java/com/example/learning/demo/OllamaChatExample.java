package com.example.learning.demo;

import com.google.common.collect.Lists;

public class OllamaChatExample {

    public static void main(String[] args) {
        OllamaClient client = new OllamaClient();
        ChatBody.ChatMessage systemMessage = ChatBody.ChatMessage.builder()
                .content("你是一名翻译专家，擅长中英文互译。")
                .role("system").build();
        ChatBody.ChatMessage userMessage = ChatBody.ChatMessage.builder()
                .content("请帮我翻译一下：但愿人长久，千里共婵娟。")
                .role("user").build();
        ChatBody body = ChatBody.builder()
                .messages(Lists.newArrayList(systemMessage, userMessage))
                .model("qwen2.5:7b")
                .stream(false).build();
        client.send(body);
    }
}
