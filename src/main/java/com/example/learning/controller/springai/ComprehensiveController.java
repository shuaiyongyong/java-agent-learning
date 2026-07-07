package com.example.learning.controller.springai;

import com.example.learning.service.springai.ComprehensiveChatService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 综合助手控制器（Spring AI 版），整合天气查询、数学计算、时间查询三大工具。
 */
@RestController
@RequestMapping("/comprehensive")
public class ComprehensiveController {

    @Resource
    private ComprehensiveChatService comprehensiveChatService;

    /**
     * 非流式聊天接口。
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return comprehensiveChatService.chat(message);
    }

    /**
     * 流式聊天接口（SSE）。
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(60_000L);

        comprehensiveChatService.chatStream(message)
                .doOnNext(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .doOnComplete(emitter::complete)
                .doOnError(emitter::completeWithError)
                .subscribe();

        return emitter;
    }
}
