package com.example.learning.service.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Spring AI 多轮对话服务（基于 ChatMemory 文件持久化）。
 * <p>
 * 在 Service 层手动管理 ChatMemory：
 * 1. 调用前从记忆加载历史消息
 * 2. 将用户消息追加到历史中发送给模型
 * 3. 收到模型回复后，将用户+助手消息对写入记忆
 */
@Service("chatMemoryService")
public class ChatMemoryService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatMemoryService(
            @org.springframework.beans.factory.annotation.Qualifier("chatMemoryClient") ChatClient chatClient,
            ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * 非流式聊天，自动加载历史并持久化对话。
     */
    public String chat(String sessionId, String message) {
        // 1. 从记忆加载该会话的历史消息
        List<Message> history = chatMemory.get(sessionId);

        // 2. 构建请求：拼入历史上下文 + 当前用户消息
        //    注意：StringBuilder 必须是局部变量，因为 Service 是 Spring 单例
        StringBuilder promptText = new StringBuilder();
        for (Message msg : history) {
            promptText.append("[").append(msg.getMessageType()).append("] ")
                      .append(msg.getText() != null ? msg.getText() : "").append("\n");
        }
        promptText.append("[USER] ").append(message);

        String response = chatClient
                .prompt()
                .user(promptText.toString())
                .call()
                .content();

        if (response != null) {
            // 3. 将本轮用户 + 助手消息写入记忆
            chatMemory.add(sessionId, List.of(
                    new org.springframework.ai.chat.messages.UserMessage(message),
                    new org.springframework.ai.chat.messages.AssistantMessage(response)
            ));
        }

        return response != null ? response : "(空回复)";
    }

    /**
     * 流式聊天，返回 Flux<String>。
     */
    public Flux<String> chatStream(String sessionId, String message) {
        // 同样加载历史
        List<Message> history = chatMemory.get(sessionId);
        StringBuilder promptText = new StringBuilder();
        for (Message msg : history) {
            promptText.append("[").append(msg.getMessageType()).append("] ")
                      .append(msg.getText() != null ? msg.getText() : "").append("\n");
        }
        promptText.append("[USER] ").append(message);

        // 收集完整响应后再保存
        StringBuilder fullResponse = new StringBuilder();
        Flux<String> flux = chatClient
                .prompt()
                .user(promptText.toString())
                .stream()
                .content();

        return flux;
    }
}
