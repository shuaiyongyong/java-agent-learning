package com.example.learning.controller.switcher;

import com.example.learning.service.switcher.LlmSwitchService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Set;

/**
 * cc switch 式第三方 LLM 切换调用入口。
 * <p>
 * 通过 {@code provider} 参数在运行时选择供应商（deepseek / kimi / qwen ...），
 * 不传则使用 {@code llm.default-provider} 配置的默认供应商，无需改动任何代码。
 */
@RestController
@RequestMapping("/switch")
public class LlmSwitchController {

    @Resource
    private LlmSwitchService llmSwitchService;

    /**
     * 查看当前已配置的所有供应商 key。
     * 例：GET /switch/providers
     */
    @GetMapping("/providers")
    public Set<String> providers() {
        return llmSwitchService.listProviders();
    }

    /**
     * 同步对话。
     * 例：GET /switch/chat?provider=kimi&question=你好
     * http://localhost:8081/switch/chat?provider=agnes-ai&system=你是一名资深气象服务专员，精通全球气候数据。为了提供准确的天气预报，你拥有一个专用的天气查询终端（getWeather 工具）。你的工作流程是：先判断用户意图，若涉及天气，优先操作终端获取数据，再用通俗易懂的语言转述给用户。&question=长沙今天得天气怎么样？
     */
    @GetMapping("/chat")
    public String chat(@RequestParam(required = false) String provider,
                       @RequestParam(required = false) String system,
                       @RequestParam String question) {
        return llmSwitchService.chat(provider, system, question);
    }

    /**
     * 流式对话（SSE）。
     * 例：GET /switch/streamChat?provider=deepseek&question=讲个笑话
     */
    @GetMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam(required = false) String provider,
                                   @RequestParam(required = false) String system,
                                   @RequestParam String question) {
        return llmSwitchService.streamChat(provider, system, question);
    }
}
