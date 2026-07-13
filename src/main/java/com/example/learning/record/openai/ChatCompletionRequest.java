package com.example.learning.record.openai;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * OpenAI 兼容的 /v1/chat/completions 请求体。
 * DeepSeek、Kimi(Moonshot)、通义、智谱、OpenAI 等主流厂商都遵循此格式，
 * 因此同一份 DTO 即可请求任意 provider，只需切换 base-url / api-key / model。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionRequest(
        String model,
        List<Message> messages,
        Double temperature,
        Boolean stream
) {
    /**
     * 单条对话消息。role 取值：system / user / assistant。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Message(String role, String content) {
    }
}
