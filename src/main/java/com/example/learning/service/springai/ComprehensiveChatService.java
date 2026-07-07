package com.example.learning.service.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

/**
 * 综合聊天服务（Spring AI 版），整合天气查询、数学计算、时间查询三大工具。
 * <p>
 * 通过注入的 {@code comprehensiveClient} 调用 Ollama 模型，
 * 模型会自动匹配已注册的 Tool（ComprehensiveToolService 中的方法）。
 */
@Service
public class ComprehensiveChatService {

    private final ChatClient comprehensiveClient;

    public ComprehensiveChatService(@Qualifier("comprehensiveClient") ChatClient comprehensiveClient) {
        this.comprehensiveClient = comprehensiveClient;
    }

    /**
     * 非流式聊天接口。
     */
    public String chat(String message) {
        return comprehensiveClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式聊天接口，返回 Flux 逐块接收回复。
     */
    public Flux<String> chatStream(String message) {
        return comprehensiveClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
