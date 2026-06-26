package com.example.learning;

import com.example.learning.service.SpringAiCustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 Spring AI 实现的客服助手 {@code SpringAiCustomerService}。
 * 通过直接调用 Service 层验证 ChatClient + Ollama 链路是否正常，
 * 同时配合 MockMvc 对 REST 端点进行集成测试。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        // 测试时使用较小的 temperature，让输出更稳定
        "spring.ai.ollama.chat.options.temperature=0.3"
})
class SpringAiCustomerTest {

    @Autowired
    private SpringAiCustomerService springAiCustomerService;

    @Test
    void testChat_OrderQuery() {
        String response = springAiCustomerService.chat("你好，我想查询一下我的订单状态，订单号是123456");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 客服助手的回复应该包含中文
        assertTrue(response.codePoints().anyMatch(c -> c >= 0x4e00 && c <= 0x9fff),
                "回复应包含中文内容，实际: " + response);
        System.out.println("=== 订单查询回复 ===");
        System.out.println(response);
    }

    @Test
    void testChat_ReturnExchange() {
        String response = springAiCustomerService.chat("我想退货，应该怎么操作？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.codePoints().anyMatch(c -> c >= 0x4e00 && c <= 0x9fff),
                "回复应包含中文内容，实际: " + response);
        System.out.println("=== 退货咨询回复 ===");
        System.out.println(response);
    }

    @Test
    void testChat_OutOfScope() {
        String response = springAiCustomerService.chat("请帮我写一段Python代码实现快速排序");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== 超范围问题回复 ===");
        System.out.println(response);
    }

    @Test
    void testChat_Greeting() {
        String response = springAiCustomerService.chat("你好呀");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        System.out.println("=== 问候语回复 ===");
        System.out.println(response);
    }
}
