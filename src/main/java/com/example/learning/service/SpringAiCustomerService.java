package com.example.learning.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 基于 Spring AI ChatClient 的客服助手。
 * <p>
 * 与 LangChain4j {@code CustomerServiceAssistant} 接口功能一致，
 * 通过注入名为 {@code customerServiceClient} 的 {@link ChatClient} Bean，
 * 复用同一套系统提示词实现客服角色行为。
 */
@Service
public class SpringAiCustomerService {

    private final ChatClient customerServiceClient;

    public SpringAiCustomerService(@Qualifier("customerServiceClient") ChatClient customerServiceClient) {
        this.customerServiceClient = customerServiceClient;
    }

    /**
     * 接收用户消息，返回客服助手的回复。
     */
    public String chat(String userMessage) {
        return customerServiceClient
                .prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
