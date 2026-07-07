package com.example.learning.controller.springai;

import com.example.learning.service.springai.SpringAiCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Spring AI 实现的客服助手控制器。
 * <p>
 * 对应 LangChain4j {@code AssistantController#customer()} 端点，
 * 路由路径为 {@code /assistant/springai}。
 */
@Slf4j
@RestController
@RequestMapping("/springai")
public class SpringAiCustomerController {

    private final SpringAiCustomerService springAiCustomerService;

    public SpringAiCustomerController(SpringAiCustomerService springAiCustomerService) {
        this.springAiCustomerService = springAiCustomerService;
    }

    /**
     * 非流式聊天接口。
     */
    @GetMapping("/chat")
    public String springaiChat(@RequestParam String message) {
        return springAiCustomerService.chat(message);
    }

    /**
     * 流式聊天接口（SSE）。
     */
    @GetMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> springaiStream(@RequestParam String message) {
        return springAiCustomerService.chatStream(message);
    }
}
