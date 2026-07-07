package com.example.learning.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.TokenStream;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 综合助手（LangChain4j 版），整合天气查询、数学计算、时间查询三大能力。
 * <p>
 * 通过 {@code tools()} 参数注入 {@code comprehensiveToolService}，
 * LangChain4j 会自动扫描其中的 {@code @Tool} 方法并在对话时将它们暴露给大模型。
 * 当用户提出天气查询、数学计算或时间相关问题时，模型会自主决定调用相应的工具。
 */
@AiService(wiringMode = EXPLICIT,
        chatModel = "langchainOllamaChatModel",
        streamingChatModel = "langchainOllamaStreamingChatModel",
        tools = {"comprehensiveToolService"})
public interface ComprehensiveAssistant {

    @SystemMessage("""
            你是一个全能助手，具备以下三种能力：
            1. 天气查询：调用 getWeather 工具获取指定城市的天气信息。
               - 只要用户要求查天气，必须调用工具，不要直接用自然语言回复。
               - 如果工具返回错误或未收录该城市，如实告知用户。
            2. 数学计算：调用 add / subtract / multiply / divide 工具完成四则运算。
               - 涉及加减乘除计算时，优先调用工具确保结果准确。
            3. 时间查询：调用 getCurrentTime 工具获取当前日期和时间。
               - 用户问时间时，必须调用工具，不要凭猜测回答。

            回复要求：
            - 始终使用简体中文，语气友好、专业。
            - 工具返回结果后，用自然语言组织最终答案回复用户。
            - 如果问题不属于以上三类，直接回答即可。
            """)
    String chat(String userMessage);

    /**
     * 流式聊天接口，返回 TokenStream，可通过 onPartialResponse 逐块接收回复。
     */
    TokenStream chatStream(String userMessage);
}
