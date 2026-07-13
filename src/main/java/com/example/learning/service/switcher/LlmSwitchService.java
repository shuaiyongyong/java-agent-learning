package com.example.learning.service.switcher;

import com.example.learning.config.LlmProviderPropertiesConfig;
import com.example.learning.record.openai.ChatCompletionRequest;
import com.example.learning.record.openai.ChatCompletionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

/**
 * cc switch 式的第三方 LLM 调用服务。
 * <p>
 * 所有支持的供应商都走 OpenAI 兼容的 {@code POST {base-url}/v1/chat/completions} 接口，
 * 因此本服务通过一个通用 {@link WebClient}，运行时根据传入的 provider key
 * 动态拼接 base-url、注入 api-key、指定 model，实现无需改代码即可切换供应商。
 */
@Service
public class LlmSwitchService {

    /** 流式响应的结束标记。 */
    private static final String STREAM_DONE = "[DONE]";

    private final LlmProviderPropertiesConfig properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LlmSwitchService(LlmProviderPropertiesConfig properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        // 通用 WebClient：base-url / 鉴权头在每次请求时按所选 provider 动态设置
        this.webClient = WebClient.builder().build();
    }

    /**
     * 列出所有已配置的供应商 key。
     */
    public Set<String> listProviders() {
        return properties.getProviders().keySet();
    }

    /**
     * 同步调用：切到指定供应商，返回完整回答文本。
     *
     * @param providerKey 供应商 key，为空则使用默认供应商
     * @param system      系统提示词，可为空
     * @param question    用户问题
     */
    public String chat(String providerKey, String system, String question) {
        LlmProviderPropertiesConfig.Provider provider = resolveProvider(providerKey);
        ChatCompletionRequest request = buildRequest(provider, system, question, false);

        ChatCompletionResponse response = post(provider, request)
                .bodyToMono(ChatCompletionResponse.class)
                .block();

        return extractContent(response);
    }

    /**
     * 流式调用：切到指定供应商，逐段返回回答内容（SSE）。
     *
     * @param providerKey 供应商 key，为空则使用默认供应商
     * @param system      系统提示词，可为空
     * @param question    用户问题
     */
    public Flux<String> streamChat(String providerKey, String system, String question) {
        LlmProviderPropertiesConfig.Provider provider = resolveProvider(providerKey);
        ChatCompletionRequest request = buildRequest(provider, system, question, true);

        return post(provider, request)
                .bodyToFlux(String.class)
                // 过滤掉结束标记与空行
                .filter(chunk -> StringUtils.hasText(chunk) && !STREAM_DONE.equals(chunk.trim()))
                .map(this::extractDelta)
                .filter(StringUtils::hasText);
    }

    /**
     * 根据 key 解析供应商配置；key 为空时回退到默认供应商。
     */
    private LlmProviderPropertiesConfig.Provider resolveProvider(String providerKey) {
        String key = StringUtils.hasText(providerKey) ? providerKey : properties.getDefaultProvider();
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("未指定供应商且未配置 llm.default-provider");
        }
        LlmProviderPropertiesConfig.Provider provider = properties.getProviders().get(key);
        if (provider == null) {
            throw new IllegalArgumentException("未找到供应商配置：" + key + "，可用：" + listProviders());
        }
        if (!StringUtils.hasText(provider.getBaseUrl()) || !StringUtils.hasText(provider.getApiKey())) {
            throw new IllegalStateException("供应商 " + key + " 的 base-url 或 api-key 未配置");
        }
        return provider;
    }

    /**
     * 组装 OpenAI 兼容请求体。
     */
    private ChatCompletionRequest buildRequest(LlmProviderPropertiesConfig.Provider provider,
                                               String system, String question, boolean stream) {
        var messages = new java.util.ArrayList<ChatCompletionRequest.Message>();
        if (StringUtils.hasText(system)) {
            messages.add(new ChatCompletionRequest.Message("system", system));
        }
        messages.add(new ChatCompletionRequest.Message("user", question));
        return new ChatCompletionRequest(provider.getModel(), messages, provider.getTemperature(), stream);
    }

    /**
     * 按所选供应商发起 POST 请求，动态设置 URL 与 Bearer 鉴权。
     */
    private WebClient.ResponseSpec post(LlmProviderPropertiesConfig.Provider provider, ChatCompletionRequest request) {
        return webClient.post()
                .uri(provider.getBaseUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve();
    }

    /**
     * 从非流式响应中提取回答文本。
     */
    private String extractContent(ChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return "";
        }
        ChatCompletionRequest.Message message = response.choices().get(0).message();
        return message != null ? message.content() : "";
    }

    /**
     * 从流式分片（一段 JSON）中提取增量内容 delta.content。
     */
    private String extractDelta(String chunk) {
        try {
            ChatCompletionResponse response = objectMapper.readValue(chunk, ChatCompletionResponse.class);
            List<ChatCompletionResponse.Choice> choices = response.choices();
            if (choices == null || choices.isEmpty()) {
                return "";
            }
            ChatCompletionRequest.Message delta = choices.get(0).delta();
            return delta != null && delta.content() != null ? delta.content() : "";
        } catch (Exception e) {
            // 分片可能不是完整 JSON（心跳/异常行），忽略
            return "";
        }
    }
}
