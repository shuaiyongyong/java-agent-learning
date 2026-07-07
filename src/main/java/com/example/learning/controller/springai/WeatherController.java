package com.example.learning.controller.springai;

import com.example.learning.service.springai.WeatherChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Resource
    private WeatherChatService weatherChatService;

    /**
     * SpringAI 模式
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return weatherChatService.chat(message);
    }
}
