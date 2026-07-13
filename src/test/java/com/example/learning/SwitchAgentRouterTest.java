package com.example.learning;

import com.example.learning.config.LlmProviderPropertiesConfig;
import com.example.learning.service.switcher.LlmSwitchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 cc switch 切换到 application.properties 中 {@code llm.providers.agent-router}
 * 配置去调用 LLM 的完整链路。
 * <p>
 * 设计：用 {@code @SpringBootTest} 加载<b>真实</b>的 agent-router 配置（读到它的
 * model=claude-opus-4-8、真实 api-key），但把该 provider 的 base-url 临时改指向本地
 * {@link HttpServer} stub，从而在不依赖真实网络与有效 key 的前提下，确定性地断言：
 * 切到 agent-router 时，确实用它自己的 model 与 api-key 发起了请求。
 * <p>
 * 真实网络调用见（可选、需有效 key）：手工把 base-url 恢复即可。
 */
@SpringBootTest
class SwitchAgentRouterTest {

    /** 目标供应商 key，对应 llm.providers.agent-router.* 配置。 */
    private static final String PROVIDER = "agent-router";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LlmSwitchService llmSwitchService;

    @Autowired
    private LlmProviderPropertiesConfig properties;

    private HttpServer server;
    /** stub 记录最近一次收到的请求信息。 */
    private final Map<String, String> lastRequest = new ConcurrentHashMap<>();
    /** agent-router 原始 base-url，测试结束后恢复，避免污染同一 Spring 上下文的其他测试。 */
    private String originalBaseUrl;

    @BeforeEach
    void setUp() throws IOException {
        // 启动本地 stub，充当 OpenAI 兼容端点并记录请求
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

        // 把 agent-router 的 base-url 临时指向本地 stub，model / api-key 保持配置文件里的真实值
        LlmProviderPropertiesConfig.Provider provider = properties.getProviders().get(PROVIDER);
        assertNotNull(provider, "application.properties 中应存在 llm.providers.agent-router 配置");
        originalBaseUrl = provider.getBaseUrl();
        provider.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        // 恢复真实 base-url，避免影响共享 Spring 上下文的其它测试
        if (originalBaseUrl != null) {
            properties.getProviders().get(PROVIDER).setBaseUrl(originalBaseUrl);
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void agentRouterProvider_isLoadedFromProperties() {
        assertTrue(llmSwitchService.listProviders().contains(PROVIDER),
                "已配置的供应商中应包含 agent-router，实际: " + llmSwitchService.listProviders());

        LlmProviderPropertiesConfig.Provider provider = properties.getProviders().get(PROVIDER);
        // base-url 已在 setUp 中改指向 stub，这里校验其余从配置文件加载的真实值
        assertEquals("claude-opus-4-8", provider.getModel(), "agent-router 模型应为 claude-opus-4-8");
        assertNotNull(provider.getApiKey(), "agent-router 的 api-key 不应为空");
        assertFalse(provider.getApiKey().isBlank(), "agent-router 的 api-key 不应为空白");
        assertEquals("https://agentrouter.org", originalBaseUrl, "agent-router 原始 base-url 应为 https://agentrouter.org");
    }

    @Test
    void chat_viaAgentRouter_usesAgentRouterModelAndKey() {
        LlmProviderPropertiesConfig.Provider provider = properties.getProviders().get(PROVIDER);

        // 切换到 agent-router 供应商发起同步调用
        String reply = llmSwitchService.chat(PROVIDER, "你是一个乐于助人的助手", "你好");

        assertEquals("stub-reply", reply);
        // 关键断言：请求确实用了 agent-router 配置里的 model 与 api-key
        assertEquals(provider.getModel(), lastRequest.get("model"),
                "请求 model 应为 agent-router 配置的 " + provider.getModel());
        assertEquals("Bearer " + provider.getApiKey(), lastRequest.get("authorization"),
                "请求鉴权头应携带 agent-router 配置的 api-key");
    }

    @Test
    void chat_viaAgentRouter_sendsSystemAndUserMessages() throws IOException {
        llmSwitchService.chat(PROVIDER, "你是一个乐于助人的助手", "你好");

        JsonNode messages = objectMapper.readTree(lastRequest.get("body")).path("messages");
        assertEquals(2, messages.size(), "应发送 system + user 两条消息");
        assertEquals("system", messages.get(0).path("role").asText());
        assertEquals("你是一个乐于助人的助手", messages.get(0).path("content").asText());
        assertEquals("user", messages.get(1).path("role").asText());
        assertEquals("你好", messages.get(1).path("content").asText());
    }
}
