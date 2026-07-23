package com.example.learning.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 的多轮对话记忆配置。
 * <p>
 * 将 {@link ChatMemory} Bean 注入到 {@link ChatClient} 的 advisor 链中，
 * 使每次对话自动加载/保存消息历史，实现类似 LangChain4j @AiService 多轮记忆的效果。
 *
 * @see com.example.learning.memory.FileChatMemory
 */
@Configuration
public class SpringAiChatMemoryConfig {

    /**
     * 基于文件的聊天记忆 Bean。
     * <p>
     * 每个 sessionId 对应一个 JSON 文件（data/chat-memory/），最多保留 10 条消息窗口。
     * 应用重启后，对话历史可从磁盘恢复。
     */
    @Bean
    public ChatMemory chatMemory() {
        return new com.example.learning.memory.FileChatMemory("data/chat-memory", 10);
    }

    /**
     * 接入 ChatMemory 的综合助手 ChatClient。
     * <p>
     * 使用 PromptChatMemoryAdvisor 自动在发送请求前加载历史消息，
     * 并在收到回复后将用户 + 助手消息对写入记忆。
     * <p>
     * 注：不绑定工具（tools），仅做纯聊天对话。
     * 因为 Spring AI 1.1.4 的 MethodToolCallbackProvider 校验 @Tool 注解时，
     * 会拒绝旧版 org.springframework.ai.tool.annotation.Tool 标注的方法，
     * 所以此处仅配置 system prompt + ChatMemory。
     */
    @Bean("chatMemoryClient")
    public ChatClient chatMemoryClient(
            OllamaChatModel chatModel,
            ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个全能助手。请用自然语言回答用户的问题，必要时提供详细的解释和步骤。")
                .defaultAdvisors(
                        org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor
                                .builder(chatMemory)
                                .build()
                )
                .build();
    }
}
