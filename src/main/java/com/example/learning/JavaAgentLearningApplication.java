package com.example.learning;

import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = OllamaAutoConfiguration.class)
public class JavaAgentLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaAgentLearningApplication.class, args);
    }
}
