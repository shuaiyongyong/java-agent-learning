package com.example.learning.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.TokenStream;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 基于 LangChain4j 的 Agent 助手，具备数学计算能力。
 * <p>
 * 通过 {@code tools()} 参数注入 {@code CalculatorService}，LangChain4j 会自动扫描其中的
 * {@code @Tool} 方法并在对话时将它们暴露给大模型。当用户提出数学问题时，模型会自主决定
 * 调用哪个工具来完成计算。
 */
@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"calculatorService"})
public interface AgentAssistant {

    @SystemMessage("""
            你是一个智能助手，具备数学计算能力。
            当用户提出数学计算相关问题时，你会使用内置的计算器工具（add / subtract / multiply / divide）完成计算，
            然后根据工具返回的结果，用自然语言回复给用户最终答案。
            如果问题不涉及计算，直接回答即可。
            始终使用简体中文回复。
            回复格式示例：
            - 用户问"3加5等于多少"，你调用 add(3,5) 得到 8，然后回复"3 加 5 等于 8"。
            - 用户问"你好"，你直接回复问候。
            """)
    String chat(String userMessage);

    /**
     * 流式聊天接口，返回 TokenStream，可通过 onPartialResponse 逐块接收回复。
     */
    TokenStream chatStream(String userMessage);
}
