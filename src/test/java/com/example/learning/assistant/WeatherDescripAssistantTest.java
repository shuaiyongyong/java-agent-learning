package com.example.learning.assistant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 {@code WeatherDescripAssistant} 中的 5 个天气助手变体（V0~V4）。
 * <p>
 * 这 5 个 {@code @AiService} 接口唯一的区别在于所绑定 {@code @Tool} 的 description
 * 措辞（基准 / 明确指令 / 场景化 / 结构化 / 极简）。本测试是一个描述措辞的对比实验：
 * 用同一批用户问题分别喂给 5 个变体，观察不同措辞对小模型「是否调用工具」的影响。
 * <p>
 * 注意：
 * <ul>
 *   <li>这些是依赖本地 Ollama 的真实 LLM 集成测试，需先启动 Ollama。</li>
 *   <li>小模型行为不稳定，断言沿用项目既有的宽松风格（工具调用痕迹 或 结果内容 二者其一即通过）。</li>
 *   <li>被测接口是包级私有的，因此本测试放在同一包 {@code com.example.learning.assistant} 下。</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        // 降低 temperature 让模型输出更稳定、更可复现
        "langchain4j.ollama.chat-model.temperature=0.1"
})
class WeatherDescripAssistantTest {

    @Autowired
    private WeatherAssistantV0 v0;
    @Autowired
    private WeatherAssistantV1 v1;
    @Autowired
    private WeatherAssistantV2 v2;
    @Autowired
    private WeatherAssistantV3 v3;
    @Autowired
    private WeatherAssistantV4 v4;

    /** 按变体名返回对应助手的 chat 方法，便于参数化测试统一驱动。 */
    private Function<String, String> clientFor(String variant) {
        return switch (variant) {
            case "V0" -> v0::chat;
            case "V1" -> v1::chat;
            case "V2" -> v2::chat;
            case "V3" -> v3::chat;
            case "V4" -> v4::chat;
            default -> throw new IllegalArgumentException("未知变体: " + variant);
        };
    }

    /** 判断回复是否体现出「调用了天气工具」——包含工具名痕迹或工具返回的天气数据。 */
    private boolean looksLikeToolWasUsed(String response, String... dataMarkers) {
        String lower = response.toLowerCase();
        boolean hasToolTrace = lower.contains("weather") || lower.contains("getweather");
        for (String marker : dataMarkers) {
            if (response.contains(marker)) {
                return true;
            }
        }
        return hasToolTrace;
    }

    private boolean containsChinese(String s) {
        return s.codePoints().anyMatch(c -> c >= 0x4e00 && c <= 0x9fff);
    }

    // ==================== 场景 1：已收录城市（北京）====================

    /**
     * 每个变体查询「北京」天气，都应体现出工具调用：
     * 要么回复里带工具名痕迹，要么带上 mock 返回的数据（28 / 晴 / 度）。
     */
    @ParameterizedTest(name = "[{0}] 查询北京天气应调用工具并给出数据")
    @ValueSource(strings = {"V0", "V1", "V2", "V3", "V4"})
    @DisplayName("已收录城市：北京天气")
    void weatherKnownCity(String variant) {
        String response = clientFor(variant).apply("请帮我查询北京的天气");
        assertNotNull(response);
        assertFalse(response.isBlank(), variant + " 返回了空回复");
        System.out.println("=== [" + variant + "] 北京天气 ===");
        System.out.println(response);

        boolean ok = looksLikeToolWasUsed(response, "北京", "28", "晴", "度");
        assertTrue(ok, "[" + variant + "] 期望包含工具调用痕迹或北京天气数据，实际: " + response);
    }

    // ==================== 场景 2：未收录城市（走 default 分支）====================

    /**
     * 查询一个 mock 未收录的城市（景德镇），工具会返回「未知 / 暂未收录该城市」。
     * 系统提示要求无论城市是否常见都要调用工具，因此仍应产出中文回复且不报错。
     */
    @ParameterizedTest(name = "[{0}] 查询未收录城市应正常回复而非报错")
    @ValueSource(strings = {"V0", "V1", "V2", "V3", "V4"})
    @DisplayName("未收录城市：景德镇（default 分支）")
    void weatherUnknownCity(String variant) {
        String response = clientFor(variant).apply("请帮我查询景德镇的天气");
        assertNotNull(response);
        assertFalse(response.isBlank(), variant + " 返回了空回复");
        System.out.println("=== [" + variant + "] 景德镇天气 ===");
        System.out.println(response);

        assertTrue(containsChinese(response), "[" + variant + "] 回复应包含中文，实际: " + response);
    }

    // ==================== 场景 3：工具异常（神秘城抛异常）====================

    /**
     * 查询「神秘城」会触发工具内部 RuntimeException（模拟服务不可用）。
     * 期望：调用不因异常中断（LangChain4j 会把异常回传给模型），
     * 模型据此给出中文的失败说明，而不是崩溃或编造温度。
     */
    @ParameterizedTest(name = "[{0}] 工具异常时应优雅降级而非崩溃")
    @ValueSource(strings = {"V0", "V1", "V2", "V3", "V4"})
    @DisplayName("工具异常：神秘城（服务不可用）")
    void weatherToolError(String variant) {
        String response = assertDoesNotThrow(
                () -> clientFor(variant).apply("请帮我查询神秘城的天气"),
                "[" + variant + "] 工具异常不应向上抛出，应由模型处理");
        assertNotNull(response);
        assertFalse(response.isBlank(), variant + " 返回了空回复");
        System.out.println("=== [" + variant + "] 神秘城天气（异常场景）===");
        System.out.println(response);

        assertTrue(containsChinese(response), "[" + variant + "] 回复应包含中文，实际: " + response);
    }

    // ==================== 场景 4：非天气问题（不应误触发工具）====================

    /**
     * 打招呼这类非天气问题，模型应正常用中文回答；此处不强制断言「未调用工具」，
     * 因为小模型可能误触发，仅验证能给出中文回复不崩溃。
     */
    @ParameterizedTest(name = "[{0}] 非天气问题应正常中文回复")
    @ValueSource(strings = {"V0", "V1", "V2", "V3", "V4"})
    @DisplayName("非天气问题：打招呼")
    void nonWeatherGreeting(String variant) {
        String response = clientFor(variant).apply("你好，请用一句话介绍你自己");
        assertNotNull(response);
        assertFalse(response.isBlank(), variant + " 返回了空回复");
        System.out.println("=== [" + variant + "] 打招呼 ===");
        System.out.println(response);

        assertTrue(containsChinese(response), "[" + variant + "] 回复应包含中文，实际: " + response);
    }

    // ==================== 场景 5：横向对比报告（实验目的）====================

    /**
     * 实验核心：用同一个问题依次喂给 5 个变体，统计各变体是否体现出工具调用，
     * 打印一份对比报告，直观展示不同 description 措辞对工具调用率的影响。
     * <p>
     * 断言较弱（至少有一个变体成功调用工具即通过），主要价值在于输出的对比表。
     */
    @Test
    @DisplayName("横向对比：5 个变体的工具调用率报告")
    void compareAllVariants() {
        String question = "上海现在天气怎么样？";
        Map<String, Boolean> report = new LinkedHashMap<>();

        for (String variant : new String[]{"V0", "V1", "V2", "V3", "V4"}) {
            String response;
            try {
                response = clientFor(variant).apply(question);
            } catch (Exception e) {
                response = "<异常: " + e.getMessage() + ">";
            }
            boolean used = looksLikeToolWasUsed(response, "上海", "26", "多云", "度");
            report.put(variant, used);
            System.out.println("=== [" + variant + "] " + question + " ===");
            System.out.println(response);
        }

        System.out.println("\n===== 工具调用对比报告（问题: " + question + "）=====");
        report.forEach((variant, used) ->
                System.out.println(variant + " -> " + (used ? "✅ 体现工具调用" : "❌ 未见工具调用痕迹")));

        long successCount = report.values().stream().filter(Boolean::booleanValue).count();
        System.out.println("成功调用工具的变体数: " + successCount + "/5");

        assertTrue(successCount >= 1,
                "期望至少有一个变体成功调用天气工具，实际全部失败。报告: " + report);
    }
}
