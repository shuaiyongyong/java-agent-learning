package com.example.learning.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient defaultClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatClient translatorClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个专业的中英翻译助手。请将用户输入的内容翻译成目标语言。如果输入是中文，则翻译成英文；如果输入是英文，则翻译成中文。只返回翻译结果，不要附加任何其他解释或评论。")
                .build();
    }
}
