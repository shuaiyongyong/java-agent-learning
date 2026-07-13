package com.example.learning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * cc switch 式的多 LLM 供应商配置。
 * <p>
 * 在 application.properties 中以 {@code llm.providers.<key>.*} 声明任意多个供应商，
 * 每个供应商只需提供 base-url / api-key / model 三要素（均为 OpenAI 兼容接口）。
 * 运行时通过 key 选择要调用的供应商，从而实现"一键切换第三方 LLM"。
 * <p>
 * 示例：
 * <pre>
 * llm.default-provider=deepseek
 * llm.providers.deepseek.base-url=https://api.deepseek.com
 * llm.providers.deepseek.api-key=sk-xxx
 * llm.providers.deepseek.model=deepseek-chat
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "llm")
@Data
public class LlmProviderPropertiesConfig {

    /** 未显式指定 provider 时使用的默认供应商 key。 */
    private String defaultProvider;

    /** 供应商配置表，key 为供应商标识（如 deepseek、kimi、qwen）。 */
    private Map<String, Provider> providers = new LinkedHashMap<>();

    @Data
    public static class Provider {
        /** OpenAI 兼容的 API 根地址，如 https://api.deepseek.com （末尾不带 /v1）。 */
        private String baseUrl;

        /** 该供应商的 API Key。 */
        private String apiKey;

        /** 默认模型名，如 deepseek-chat、moonshot-v1-8k、qwen-max。 */
        private String model;

        /** 采样温度，缺省 0.7。 */
        private Double temperature = 0.7;
    }
}
