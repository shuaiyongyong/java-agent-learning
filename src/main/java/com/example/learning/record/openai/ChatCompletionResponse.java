package com.example.learning.record.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * OpenAI 兼容的 /v1/chat/completions 响应体（仅提取常用字段）。
 * 非流式返回时使用；流式返回时每个 SSE data 分片也复用此结构（delta 放在 message 里可能为空）。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatCompletionResponse(
        String id,
        String model,
        List<Choice> choices
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(Integer index, ChatCompletionRequest.Message message, ChatCompletionRequest.Message delta) {
    }
}
