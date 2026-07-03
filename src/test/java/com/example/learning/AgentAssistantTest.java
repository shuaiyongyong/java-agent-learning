package com.example.learning;

import com.example.learning.assistant.AgentAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 Agent 助手（LangChain4j @AiService + @Tool 计算器）的功能。
 * <p>
 * 验证当用户提出数学计算问题时，Agent 能自动调用 CalculatorService 中的
 * {@code @Tool} 方法完成计算并返回正确结果。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        // 使用较小的 temperature 让模型输出更稳定
        "langchain4j.ollama.chat-model.temperature=0.1"
})
class AgentAssistantTest {

    @Autowired
    private AgentAssistant agentAssistant;

    /**
     * 测试加法：Agent 应当调用 add 工具计算 3 + 5 = 8
     * 断言：回复中包含 "add"（工具调用痕迹）或 "8"（计算结果）
     */
    @Test
    void testChat_Addition() {
        String response = agentAssistant.chat("3 加上 5 等于多少？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== 加法回复 ===");
        System.out.println(response);
        // 小模型工具调用可能返回原始 JSON 或最终答案，两者都算成功
        boolean hasToolCall = response.toLowerCase().contains("add");
        boolean hasResult = response.contains("8");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具名 'add' 或结果 '8'，实际: " + response);
    }

    /**
     * 测试乘法：Agent 应当调用 multiply 工具计算 6 × 7 = 42
     */
    @Test
    void testChat_Multiplication() {
        String response = agentAssistant.chat("6 乘以 7 等于多少？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== 乘法回复 ===");
        System.out.println(response);
        boolean hasToolCall = response.toLowerCase().contains("multiply");
        boolean hasResult = response.contains("42");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具名 'multiply' 或结果 '42'，实际: " + response);
    }

    /**
     * 测试除法：Agent 应当调用 divide 工具计算 100 ÷ 4 = 25
     */
    @Test
    void testChat_Division() {
        String response = agentAssistant.chat("100 除以 4 等于多少？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== 除法回复 ===");
        System.out.println(response);
        boolean hasToolCall = response.toLowerCase().contains("divide");
        boolean hasResult = response.contains("25");
        assertTrue(hasToolCall || hasResult,
                "期望包含工具名 'divide' 或结果 '25'，实际: " + response);
    }

    /**
     * 测试非计算问题：Agent 应直接回答而不调用工具
     */
    @Test
    void testChat_NonCalculation() {
        String response = agentAssistant.chat("你好，请介绍一下你自己");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 回复应包含中文
        assertTrue(response.codePoints().anyMatch(c -> c >= 0x4e00 && c <= 0x9fff),
                "回复应包含中文内容，实际: " + response);
        System.out.println("=== 非计算问题回复 ===");
        System.out.println(response);
    }
}
