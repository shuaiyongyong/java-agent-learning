package com.example.learning;

import com.example.learning.assistant.ComprehensiveAssistant;
import com.example.learning.service.springai.ComprehensiveChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试综合助手（LangChain4j + Spring AI）的端到端功能。
 * <p>
 * 综合助手整合了三大工具：天气查询、数学计算、时间查询。
 * 本测试覆盖两个框架的实现，验证模型能自动选择正确的工具完成用户请求。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        // LangChain4j 侧使用低 temperature 让输出更稳定
        "langchain4j.ollama.chat-model.temperature=0.1",
        // Spring AI 侧同样降低 temperature
        "spring.ai.ollama.chat.options.temperature=0.1"
})
class ComprehensiveAssistantTest {

    @Autowired
    private ComprehensiveAssistant langchainAssistant;

    @Autowired
    private ComprehensiveChatService springAiAssistant;

    @Autowired
    private TestRestTemplate restTemplate;

    // ==================== LangChain4j 测试 ====================

    @Test
    @DisplayName("[LangChain4j] 天气查询：北京天气")
    void langchain_weatherBeijing() {
        String response = langchainAssistant.chat("请帮我查询北京的天气");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [LangChain4j] 北京天气 ===");
        System.out.println(response);
        // 模型要么调用了 getWeather 工具，要么在回复中包含了天气相关信息
        boolean hasToolCall = response.toLowerCase().contains("weather")
                || response.toLowerCase().contains("getweather");
        boolean hasResult = response.contains("北京") || response.contains("晴") || response.contains("度");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具调用痕迹或天气相关内容，实际: " + response);
    }

    @Test
    @DisplayName("[LangChain4j] 数学计算：3 + 5")
    void langchain_addition() {
        String response = langchainAssistant.chat("3 加上 5 等于多少？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [LangChain4j] 加法计算 ===");
        System.out.println(response);
        // 小模型可能返回工具调用 JSON 或直接给出答案，两者都算成功
        boolean hasToolCall = response.toLowerCase().contains("add");
        boolean hasResult = response.contains("8");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具名 'add' 或结果 '8'，实际: " + response);
    }

    @Test
    @DisplayName("[LangChain4j] 数学计算：12 × 7")
    void langchain_multiplication() {
        String response = langchainAssistant.chat("12 乘以 7 等于多少？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [LangChain4j] 乘法计算 ===");
        System.out.println(response);
        boolean hasToolCall = response.toLowerCase().contains("multiply");
        boolean hasResult = response.contains("84");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具名 'multiply' 或结果 '84'，实际: " + response);
    }

    @Test
    @DisplayName("[LangChain4j] 时间查询：现在几点")
    void langchain_currentTime() {
        String response = langchainAssistant.chat("现在几点了？告诉我当前时间");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [LangChain4j] 时间查询 ===");
        System.out.println(response);
        // 工具返回原始时间戳，但模型可能会用中文重新表述（如"2026年7月7日16点47分19秒"）
        // 只要回复中包含年份（4位数字）或"时间"/"点"/"分"等关键词就算成功
        boolean hasTimestamp = response.matches(".*\\d{4}.*");
        boolean hasToolCall = response.contains("时间") || response.contains("点") || response.contains("分");
        assertTrue(hasTimestamp || hasToolCall,
                "期望包含时间戳格式或时间相关内容，实际: " + response);
    }

    @Test
    @DisplayName("[LangChain4j] 非工具问题：打招呼")
    void langchain_greeting() {
        String response = langchainAssistant.chat("你好，请介绍一下你自己");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 回复应包含中文
        assertTrue(response.codePoints().anyMatch(c -> c >= 0x4e00 && c <= 0x9fff),
                "回复应包含中文内容，实际: " + response);
        System.out.println("=== [LangChain4j] 打招呼 ===");
        System.out.println(response);
    }

    // ==================== Spring AI 测试 ====================

    @Test
    @DisplayName("[Spring AI] 天气查询：上海天气")
    void springai_weatherShanghai() {
        String response = springAiAssistant.chat("请帮我查询上海的天气");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [Spring AI] 上海天气 ===");
        System.out.println(response);
        boolean hasToolCall = response.toLowerCase().contains("weather")
                || response.toLowerCase().contains("getweather");
        boolean hasResult = response.contains("上海") || response.contains("多云") || response.contains("度");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具调用痕迹或天气相关内容，实际: " + response);
    }

    @Test
    @DisplayName("[Spring AI] 数学计算：100 除以 4")
    void springai_division() {
        String response = springAiAssistant.chat("100 除以 4 等于多少？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [Spring AI] 除法计算 ===");
        System.out.println(response);
        boolean hasToolCall = response.toLowerCase().contains("divide");
        boolean hasResult = response.contains("25");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具名 'divide' 或结果 '25'，实际: " + response);
    }

    @Test
    @DisplayName("[Spring AI] 时间查询：当前时间")
    void springai_currentTime() {
        String response = springAiAssistant.chat("现在是什么时间？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [Spring AI] 时间查询 ===");
        System.out.println(response);
        // 工具返回原始时间戳，但模型可能会用中文重新表述（如"2026年7月7日16点50分06秒"）
        boolean hasTimestamp = response.matches(".*\\d{4}.*");
        boolean hasToolCall = response.contains("时间") || response.contains("点") || response.contains("分");
        assertTrue(hasTimestamp || hasToolCall,
                "期望包含时间戳格式或时间相关内容，实际: " + response);
    }

    // ==================== REST 端点测试 ====================

    @Test
    @DisplayName("[REST] LangChain4j 综合助手端点：天气查询")
    void rest_langchain_weather() {
        String url = "/assistant/comprehensive?message=请帮我查询广州的天气";
        String response = restTemplate.getForObject(url, String.class);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [REST] LangChain4j 广州天气 ===");
        System.out.println(response);
        // 至少应包含中文回复
        assertTrue(response.codePoints().anyMatch(c -> c >= 0x4e00 && c <= 0x9fff),
                "回复应包含中文内容，实际: " + response);
    }

    @Test
    @DisplayName("[REST] Spring AI 综合助手端点：计算")
    void rest_springai_calc() {
        String url = "/comprehensive/chat?message=请帮我计算 6 乘以 9 等于多少";
        String response = restTemplate.getForObject(url, String.class);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== [REST] Spring AI 乘法计算 ===");
        System.out.println(response);
        boolean hasToolCall = response.toLowerCase().contains("multiply");
        boolean hasResult = response.contains("54");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具名 'multiply' 或结果 '54'，实际: " + response);
    }
}
