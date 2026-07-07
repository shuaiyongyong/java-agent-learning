package com.example.learning.controller.springai;

import com.example.learning.service.springai.TranslationChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/translation")
public class TranslationChatController {

    @Resource
    private TranslationChatService translationService;


    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return translationService.chat(message);
    }

}
