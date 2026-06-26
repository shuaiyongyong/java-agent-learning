package com.example.learning.controller;

import com.example.learning.service.SpringAiCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring AI 实现的客服助手控制器。
 * <p>
 * 对应 LangChain4j {@code AssistantController#customer()} 端点，
 * 路由路径为 {@code /assistant/springai}。
 */
@Slf4j
@RestController
@RequestMapping("/assistant")
public class SpringAiCustomerController {

    private final SpringAiCustomerService springAiCustomerService;

    public SpringAiCustomerController(SpringAiCustomerService springAiCustomerService) {
        this.springAiCustomerService = springAiCustomerService;
    }

    @GetMapping("/springai")
    public String springaiChat(@RequestParam String message) {
        return springAiCustomerService.chat(message);
    }
}
