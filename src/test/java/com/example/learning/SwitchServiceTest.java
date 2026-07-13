package com.example.learning;

import com.example.learning.config.switcher.LlmProviderProperties;
import com.example.learning.service.switcher.LlmSwitchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 cc switch 式供应商切换逻辑 {@link LlmSwitchService}。
 * <p>
 * 不依赖真实第三方 LLM：用 JDK 内置 {@link HttpServer} 起一个本地 stub 充当
 * OpenAI 兼容端点，记录每次收到的请求（model / Authorization / body），
 * 从而确定性地断言"传入不同 provider 是否正确切到对应的 base-url、model、api-key"。
 */
class SwitchServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpServer server;
    /** stub 记录最近一次收到的请求信息，供断言使用。 */
    private final Map<String, String> lastRequest = new ConcurrentHashMap<>();

    private LlmSwitchService switchService;
    private LlmProviderProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        // 1. 启动本地 stub：固定回复一段 OpenAI 兼容的非流式响应，并记录请求
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            lastRequest.put("body", body);
            lastRequest.put("authorization", exchange.getRequestHeaders().getFirst("Authorization"));
            JsonNode node = objectMapper.readTree(body);
            lastRequest.put("model", node.path("model").asText());

            byte[] resp = ("""
                    {"id":"stub-1","model":"%s","choices":[{"index":0,"message":{"role":"assistant","content":"stub-reply"}}]}
                    """.formatted(node.path("model").asText())).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });
        server.start();
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();

        // 2. 手工构建两家供应商配置，均指向本地 stub，但 model / api-key 不同，
        //    以此证明切换时确实使用了各自 provider 的配置
        properties = new LlmProviderProperties();
        properties.setDefaultProvider("alpha");

        LlmProviderProperties.Provider alpha = new LlmProviderProperties.Provider();
        alpha.setBaseUrl(baseUrl);
        alpha.setApiKey("key-alpha");
        alpha.setModel("model-alpha");
        alpha.setTemperature(0.7);

        LlmProviderProperties.Provider beta = new LlmProviderProperties.Provider();
        beta.setBaseUrl(baseUrl);
        beta.setApiKey("key-beta");
        beta.setModel("model-beta");
        beta.setTemperature(0.7);

        properties.setProviders(Map.of("alpha", alpha, "beta", beta));

        switchService = new LlmSwitchService(properties, objectMapper);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void listProviders_returnsAllConfiguredKeys() {
        assertEquals(java.util.Set.of("alpha", "beta"), switchService.listProviders());
    }

    @Test
    void chat_withoutProvider_usesDefaultProvider() {
        String reply = switchService.chat(null, null, "你好");

        assertEquals("stub-reply", reply);
        // 未传 provider 时应回退到默认供应商 alpha，故 stub 收到的 model 应为 model-alpha
        assertEquals("model-alpha", lastRequest.get("model"));
        assertEquals("Bearer key-alpha", lastRequest.get("authorization"));
    }

    @Test
    void chat_switchProvider_usesTargetProviderConfig() {
        String reply = switchService.chat("beta", null, "你好");

        assertEquals("stub-reply", reply);
        // 切到 beta：stub 收到的 model 与鉴权头都应来自 beta 的配置
        assertEquals("model-beta", lastRequest.get("model"));
        assertEquals("Bearer key-beta", lastRequest.get("authorization"));
    }

    @Test
    void chat_withSystemPrompt_prependsSystemMessage() throws IOException {
        switchService.chat("alpha", "你是翻译官", "hello");

        // 请求体应包含 system 与 user 两条消息，且 system 在前
        JsonNode messages = objectMapper.readTree(lastRequest.get("body")).path("messages");
        assertEquals(2, messages.size());
        assertEquals("system", messages.get(0).path("role").asText());
        assertEquals("你是翻译官", messages.get(0).path("content").asText());
        assertEquals("user", messages.get(1).path("role").asText());
        assertEquals("hello", messages.get(1).path("content").asText());
    }

    @Test
    void chat_withoutSystemPrompt_onlyUserMessage() throws IOException {
        switchService.chat("alpha", null, "hello");

        JsonNode messages = objectMapper.readTree(lastRequest.get("body")).path("messages");
        assertEquals(1, messages.size());
        assertEquals("user", messages.get(0).path("role").asText());
    }

    @Test
    void chat_unknownProvider_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> switchService.chat("not-exist", null, "你好"));
        assertTrue(ex.getMessage().contains("not-exist"), "异常信息应包含未知的供应商 key");
    }

    @Test
    void chat_noProviderAndNoDefault_throws() {
        properties.setDefaultProvider(null);
        assertThrows(IllegalArgumentException.class,
                () -> switchService.chat(null, null, "你好"));
    }

    @Test
    void chat_missingApiKey_throws() {
        LlmProviderProperties.Provider broken = new LlmProviderProperties.Provider();
        broken.setBaseUrl("http://127.0.0.1:1");
        broken.setModel("m");
        // 故意不设置 api-key
        properties.setProviders(Map.of("broken", broken));

        assertThrows(IllegalStateException.class,
                () -> switchService.chat("broken", null, "你好"));
    }
}
