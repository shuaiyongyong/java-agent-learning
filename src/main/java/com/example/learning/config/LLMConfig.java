package com.example.learning.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    // 模型名称常量定义，统一管理，避免硬编码
    private static final String DEEPSEEK_MODEL = "deepseek-v3";

    private static final String QWEN_MODEL = "qwen-max";

    /**
     * DeepSeek-V3 ChatModel 实例
     * 通过阿里云百炼API调用，无需单独申请DeepSeek Key
     */
    @Bean(name = "deepseek")
    public ChatModel deepSeekChatModel() {
        return DashScopeChatModel.builder()
                // 从系统环境变量读取API Key，避免硬编码泄露
                .dashScopeApi(DashScopeApi.builder()
                        .apiKey(apiKey)
                        .build())
                // 全局默认模型参数，统一管理
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel(DEEPSEEK_MODEL)
                        .withTemperature(0.7)
                        .maxToken(2000)
                        .build())
                .build();
    }

    /**
     * Qwen-Max ChatModel 实例
     */
    @Bean(name = "qwen")
    public ChatModel qwenChatModel() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder()
                        .apiKey(apiKey)
                        .build())
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel(QWEN_MODEL)
                        .withTemperature(0.7)
                        .maxToken(2000)
                        .build())
                .build();
    }

    /**
     * DeepSeek-V3 ChatClient 实例
     * 基于已注册的deepseek ChatModel构建
     */
    @Bean(name = "deepseekChatClient")
    public ChatClient deepseekChatClient(@Qualifier("deepseek") ChatModel deepseek) {
        return ChatClient.builder(deepseek)
                // 可选：全局默认系统提示词，所有调用都会自动携带
                .defaultSystem("你是一个专业的AI助手，回答问题简洁、高效、有逻辑")
                .build();
    }

    /**
     * Qwen-Max ChatClient 实例
     * 基于已注册的qwen ChatModel构建
     */
    @Bean(name = "qwenChatClient")
    public ChatClient qwenChatClient(@Qualifier("qwen") ChatModel qwen) {
        return ChatClient.builder(qwen)
                .defaultSystem("你是一个专业的Java后端开发工程师，擅长Spring生态技术栈，回答问题专业、有可落地的代码示例")
                .build();
    }
}
