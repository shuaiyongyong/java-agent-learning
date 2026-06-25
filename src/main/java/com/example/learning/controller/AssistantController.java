package com.example.learning.controller;

import com.example.learning.service.CustomerServiceAssistant;
import com.example.learning.service.OllamaAssistant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/assistant")
public class AssistantController {

    @Autowired
    OllamaAssistant assistant;

    @Autowired
    CustomerServiceAssistant customerServiceAssistant;

    @GetMapping("/chat")
    public String chat(String message) {
        return assistant.chat(message);
    }

    @GetMapping("/customer")
    public String customer(String message) {
        return customerServiceAssistant.chat(message);
    }
}
