package com.example.learning.controller;

import com.example.learning.assistant.AgentAssistant;
import com.example.learning.assistant.CustomerAssistant;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/assistant")
public class AssistantController {

    @Resource
    CustomerAssistant customerAssistant;

    @Resource
    AgentAssistant agentAssistant;

    /**
     * 非流式聊天接口（LangChain4j @AiService）。
     */
    @GetMapping("/customer")
    public String customer(@RequestParam String message) {
        return customerAssistant.chat(message);
    }

    /**
     * 流式聊天接口（SSE），基于 LangChain4j TokenStream。
     */
    @GetMapping(value = "/customer/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter customerStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(60_000L);

        TokenStream tokenStream = customerAssistant.chatStream(message);
        tokenStream
                .onPartialResponse(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(resp -> emitter.complete())
                .onError(emitter::completeWithError)
                .start();

        return emitter;
    }

    /**
     * Agent 聊天接口（LangChain4j @AiService + @Tool 计算器）。
     */
    @GetMapping("/agent")
    public String agent(@RequestParam String message) {
        return agentAssistant.chat(message);
    }

    /**
     * Agent 流式聊天接口（SSE），基于 LangChain4j TokenStream。
     */
    @GetMapping(value = "/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter agentStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(60_000L);

        TokenStream tokenStream = agentAssistant.chatStream(message);
        tokenStream
                .onPartialResponse(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(resp -> emitter.complete())
                .onError(emitter::completeWithError)
                .start();

        return emitter;
    }
}
