package com.example.learning.controller.langchain;

import com.example.learning.assistant.ComprehensiveAssistant;
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
@RequestMapping("/comprehensive/assistant")
public class ComprehensiveAssistantController {

    @Resource
    ComprehensiveAssistant comprehensiveAssistant;

    /**
     * 综合助手聊天接口（LangChain4j @AiService + 天气/计算/时间三大工具）。
     */
    @GetMapping("/comprehensive")
    public String comprehensive(@RequestParam String message) {
        return comprehensiveAssistant.chat(message);
    }

    /**
     * 综合助手流式聊天接口（SSE），基于 LangChain4j TokenStream。
     */
    @GetMapping(value = "/comprehensive/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter comprehensiveStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(60_000L);

        TokenStream tokenStream = comprehensiveAssistant.chatStream(message);
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
