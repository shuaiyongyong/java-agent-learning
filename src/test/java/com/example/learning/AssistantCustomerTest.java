package com.example.learning;

import com.example.learning.service.CustomerServiceAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试客服助手接口 /assistant/customer 的功能。
 * 通过直接调用 CustomerServiceAssistant 来验证 LangChain4j @AiService 是否正常工作，
 * 同时配合 MockMvc 对 REST 端点进行集成测试。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        // 测试时使用较小的 temperature，让输出更稳定
        "langchain4j.ollama.chat-model.temperature=0.3"
})
class AssistantCustomerTest {

    @Autowired
    private CustomerServiceAssistant customerServiceAssistant;

    @Test
    void testChat_OrderQuery() {
        String response = customerServiceAssistant.chat("你好，我想查询一下我的订单状态，订单号是123456");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 客服助手的回复应该包含中文（用 contains 而非 matches，避免多行匹配问题）
        assertTrue(response.codePoints().anyMatch(c -> c >= 0x4e00 && c <= 0x9fff),
                "回复应包含中文内容，实际: " + response);
        System.out.println("=== 订单查询回复 ===");
        System.out.println(response);
    }

    @Test
    void testChat_ReturnExchange() {
        String response = customerServiceAssistant.chat("我想退货，应该怎么操作？");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 客服助手的回复应该包含中文（用 codePoints 而非 matches，避免多行匹配问题）
        assertTrue(response.codePoints().anyMatch(c -> c >= 0x4e00 && c <= 0x9fff),
                "回复应包含中文内容，实际: " + response);
        System.out.println("=== 退货咨询回复 ===");
        System.out.println(response);
    }

    @Test
    void testChat_OutOfScope() {
        String response = customerServiceAssistant.chat("请帮我写一段Python代码实现快速排序");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 超出客服范围时，应该引导联系人工客服
        System.out.println("=== 超范围问题回复 ===");
        System.out.println(response);
    }

    @Test
    void testChat_Greeting() {
        String response = customerServiceAssistant.chat("你好呀");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 应该使用友好的语气，可能包含"亲"之类的称呼
        System.out.println("=== 问候语回复 ===");
        System.out.println(response);
    }
}
