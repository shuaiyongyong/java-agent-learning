package com.example.learning.config;

import com.example.learning.service.langchain.WeatherService;
import com.example.learning.service.springai.SpringAiWeatherService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public OllamaChatModel ollamaChatModel(
            @Value("${spring.ai.ollama.base-url}") String baseUrl,
            @Value("${spring.ai.ollama.chat.options.model}") String model) {
        return OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(baseUrl))
                .defaultOptions(OllamaOptions.builder()
                        .model(model)
                        .build())
                .build();
    }

    @Bean
    public ChatClient defaultClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatClient translatorClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个专业的中英翻译助手。请将用户输入的内容翻译成目标语言。如果输入是中文，则翻译成英文；如果输入是英文，则翻译成中文。只返回翻译结果，不要附加任何其他解释或评论。")
                .build();
    }

    /**
     * 客服助手专用 ChatClient，内置客服系统提示词。
     */
    @Bean
    public ChatClient customerServiceClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是「悦购商城」的在线客服助手，名叫小悦。
                        职责：解答订单、物流、退换货、优惠券与账户相关的问题。
                        要求：
                        1. 始终使用简体中文，语气友好、耐心、专业，称呼用户为「亲」。
                        2. 回答简洁清晰，必要时分点说明操作步骤。
                        3. 如果问题超出电商客服范围，或需要核实用户身份/订单等隐私信息，礼貌地引导用户联系人工客服（热线 400-100-1688）。
                        4. 不要编造订单号、物流单号、优惠政策等具体信息；信息不足时主动向用户询问。
                        """)
                .build();
    }

    @Bean
    public ChatClient weatherClient(OllamaChatModel chatModel, SpringAiWeatherService weatherService) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个天气查询助手。当用户请求查询某个城市的天气时，你必须调用 getWeather 工具，无论城市名是否常见。不要拒绝调用工具，也不要直接用自然语言回复天气查询。")
                .defaultTools(ToolCallbacks.from(weatherService))
                .build();
    }
}
