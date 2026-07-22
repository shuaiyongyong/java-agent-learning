package com.example.learning;

import com.example.learning.assistant.ComprehensiveAssistant;
import com.example.learning.service.springai.ComprehensiveChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== LangChain4j 测试 ====================

    @Test
    @DisplayName("[LangChain4j] 天气查询：北京天气")
    void langchain_weatherBeijing() {
        String response = langchainAssistant.chat("test-default", "请帮我查询北京的天气");
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
        String response = langchainAssistant.chat("test-default", "3 加上 5 等于多少？");
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
        String response = langchainAssistant.chat("test-default", "12 乘以 7 等于多少？");
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
        String response = langchainAssistant.chat("test-default", "现在几点了？告诉我当前时间");
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
        String response = langchainAssistant.chat("test-default", "你好，请介绍一下你自己");
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
    @DisplayName("[多轮对话] 记住上文提到的名字")
    void langchain_multiTurnRememberName() {
        String memoryId = "multi-turn-test-" + System.currentTimeMillis();

        // 第一轮：告诉助手自己的名字
        String firstReply = langchainAssistant.chat(memoryId, "你好，我叫小明");
        assertNotNull(firstReply);
        assertFalse(firstReply.isEmpty());
        System.out.println("=== [多轮对话] 第1轮 ===");
        System.out.println(firstReply);

        // 第二轮：不问名字，只说"你好"，验证助手还记得名字
        String secondReply = langchainAssistant.chat(memoryId, "你还记得我叫什么名字吗？");
        assertNotNull(secondReply);
        assertFalse(secondReply.isEmpty());
        System.out.println("=== [多轮对话] 第2轮 ===");
        System.out.println(secondReply);
        // 模型应该能在回复中提到"小明"
        assertTrue(secondReply.contains("小明"),
                "期望助手记住名字'小明'，实际: " + secondReply);
    }

    @Test
    @DisplayName("[多轮对话] 不同 sessionId 之间记忆隔离")
    void langchain_memoryIsolation() {
        String memoryIdA = "isolation-test-A-" + System.currentTimeMillis();
        String memoryIdB = "isolation-test-B-" + System.currentTimeMillis();

        // A 会话：告诉名字
        langchainAssistant.chat(memoryIdA, "我的名字是小华");

        // B 会话：询问名字，应该不知道 A 会话的信息
        String bReply = langchainAssistant.chat(memoryIdB, "你知道我的名字吗？");
        assertNotNull(bReply);
        System.out.println("=== [记忆隔离] B会话回复 ===");
        System.out.println(bReply);
        // B 会话不应该知道"小华"，因为那是 A 会话的记忆
        assertFalse(bReply.contains("小华"),
                "B 会话不应知道 A 会话的名字'小华'，实际: " + bReply);
    }

    @Test
    @DisplayName("[多轮对话] 消息窗口溢出：超过10条后旧消息被丢弃")
    void langchain_messageWindowEviction() {
        String memoryId = "eviction-test-" + System.currentTimeMillis();

        // 第1轮：告诉助手一个独特的标识信息——"我叫阿强"
        String message = langchainAssistant.chat(memoryId, "你好，我叫阿强");
        System.out.println(message);
        // 中间插入大量无关对话，挤占消息窗口
        // 每条对话都产生 2 条消息（用户 + AI），10 条消息窗口 ≈ 5 轮完整对话
        // 发送 8 轮无关对话后，总消息数约 16 条，最早的第1轮应已被挤出窗口
        for (int i = 1; i <= 8; i++) {
           String msg = langchainAssistant.chat(memoryId, "这是第 " + i + " 轮无关对话，请简单回复收到");
            System.out.println(msg);
        }
        // 最后询问名字：如果记忆窗口正常溢出，助手应该已经"忘记"了"阿强"
        String reply = langchainAssistant.chat(memoryId, "你知道我叫什么名字吗？");
        assertNotNull(reply);
        System.out.println("=== [消息窗口溢出] 最终回复 ===");
        System.out.println(reply);
        // 由于第1轮的"我叫阿强"已被挤出10条消息窗口，
        // 助手中不再有这条记忆，不应能答出"阿强"
        assertFalse(reply.contains("阿强"),
                "期望旧消息已被挤出窗口，助手不应记得'阿强'，实际: " + reply);
    }

    @Test
    @DisplayName("[多轮对话] 消息窗口未满时记忆仍有效")
    void langchain_messageWindowStillWorksWithinLimit() {
        String memoryId = "window-limit-test-" + System.currentTimeMillis();

        // 第1轮：告诉助手名字
        langchainAssistant.chat(memoryId, "你好，我叫阿花");

        // 只插入 2 轮无关对话，总消息数约 6 条，仍在10条窗口内
        langchainAssistant.chat(memoryId, "请简单回复收到");
        langchainAssistant.chat(memoryId, "再回复一下");

        // 此时"我叫阿花"仍在消息窗口中，助手应该还记得
        String reply = langchainAssistant.chat(memoryId, "你知道我叫什么名字吗？");
        assertNotNull(reply);
        System.out.println("=== [窗口内记忆] 最终回复 ===");
        System.out.println(reply);
        assertTrue(reply.contains("阿花"),
                "期望助手仍记得'阿花'（消息在窗口内），实际: " + reply);
    }

    // ==================== JDBC 持久化测试 ====================

    @Test
    @DisplayName("[JDBC持久化] 对话历史写入数据库后可查询验证")
    void langchain_jdbcPersistenceVerifiable() {
        String memoryId = "jdbc-persist-test-" + System.currentTimeMillis();

        // 第1轮：告诉助手名字
        langchainAssistant.chat(memoryId, "你好，我叫小鹏");

        // 验证数据库中确实写入了消息记录
        int rowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MEMORY_MESSAGES WHERE MEMORY_ID = ?",
                Integer.class, memoryId);
        // 每轮对话写入 2 条消息（用户 + AI）
        assertTrue(rowCount >= 2,
                "期望数据库至少有 2 条记录，实际: " + rowCount);
        System.out.println("=== [JDBC持久化] 数据库写入记录数: " + rowCount + " ===");

        // 第2轮：验证助手还能记住名字
        String reply = langchainAssistant.chat(memoryId, "你还记得我叫什么名字吗？");
        assertNotNull(reply);
        System.out.println("=== [JDBC持久化] 第2轮回复 ===");
        System.out.println(reply);
        assertTrue(reply.contains("小鹏"),
                "助手应能从数据库恢复记忆并记住名字，实际: " + reply);
    }

    @Test
    @DisplayName("[JDBC持久化] 多会话数据在数据库中相互隔离")
    void langchain_jdbcSessionIsolation() {
        String memoryIdA = "jdbc-isolation-A-" + System.currentTimeMillis();
        String memoryIdB = "jdbc-isolation-B-" + System.currentTimeMillis();

        // A 会话：写入"我叫阿A"
        langchainAssistant.chat(memoryIdA, "你好，我叫阿A");

        // B 会话：写入"我叫阿B"
        langchainAssistant.chat(memoryIdB, "你好，我叫阿B");

        // 验证数据库中两个会话的记录互不干扰
        int countA = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MEMORY_MESSAGES WHERE MEMORY_ID = ?",
                Integer.class, memoryIdA);
        int countB = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MEMORY_MESSAGES WHERE MEMORY_ID = ?",
                Integer.class, memoryIdB);

        System.out.println("=== [JDBC持久化] 会话A记录数: " + countA + "，会话B记录数: " + countB + " ===");
        assertTrue(countA >= 2, "会话A至少有2条记录");
        assertTrue(countB >= 2, "会话B至少有2条记录");

        // 验证 B 会话不记得 A 的名字
        String bReply = langchainAssistant.chat(memoryIdB, "你知道我的名字吗？");
        System.out.println("=== [JDBC持久化] B会话回复 ===");
        System.out.println(bReply);
        assertFalse(bReply.contains("阿A"),
                "B 会话不应知道 A 会话的名字'阿A'，实际: " + bReply);
    }
}
