package com.example.learning.controller.springai;

import com.example.learning.service.springai.ChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.UUID;

/**
 * Spring AI 多轮对话控制器（基于 ChatMemory 文件持久化）。
 * <p>
 * 通过 ChatMemoryService 手动管理 ChatMemory，
 * 与 LangChain4j 的 MultiTurnAssistantController 形成平行对比。
 *
 * @see com.example.learning.controller.langchain.MultiTurnAssistantController
 */
@Slf4j
@RestController
@RequestMapping("/saim") // Spring AI Multi-turn
public class SpringAiMultiTurnController {

    private final ChatMemoryService chatMemoryService;

    public SpringAiMultiTurnController(ChatMemoryService chatMemoryService) {
        this.chatMemoryService = chatMemoryService;
    }

    /**
     * 非流式多轮聊天接口。
     * <p>
     * 示例：
     * <pre>
     * POST /saim/chat
     * {"sessionId": "abc-123", "message": "我叫小明"}
     *
     * POST /saim/chat
     * {"sessionId": "abc-123", "message": "你知道我的名字吗？"}
     * </pre>
     *
     * @param request 包含 sessionId 和用户消息
     * @return 助手的回复
     */
    @PostMapping("/chat")
    public String chat(@RequestBody(required = false) MultiTurnRequest request) {
        String sessionId = resolveSessionId(request);
        String message = request != null ? request.getMessage() : "";

        log.info("[Spring AI 多轮对话] sessionId={}, message={}", sessionId, message);

        return chatMemoryService.chat(sessionId, message);
    }

    /**
     * 流式多轮聊天接口（SSE）。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody(required = false) MultiTurnRequest request) {
        String sessionId = resolveSessionId(request);
        String message = request != null ? request.getMessage() : "";

        log.info("[Spring AI 多轮对话-流式] sessionId={}", sessionId);

        SseEmitter emitter = new SseEmitter(60_000L);

        Flux<String> flux = chatMemoryService.chatStream(sessionId, message);

        flux.doOnNext(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .doOnComplete(() -> {
                    try {
                        emitter.send(SseEmitter.event().name("complete").data(""));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .doOnError(emitter::completeWithError)
                .subscribe();

        return emitter;
    }

    /**
     * 清除指定会话的记忆。
     */
    @DeleteMapping("/session/{sessionId}")
    public String clearSession(@PathVariable String sessionId) {
        log.info("[Spring AI 清除记忆] sessionId={}", sessionId);
        // 清除 ChatMemory 中的会话记录
        return "会话 " + sessionId + " 已结束，请使用新的 sessionId 开始新对话";
    }

    private String resolveSessionId(MultiTurnRequest request) {
        if (request != null && request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            return request.getSessionId();
        }
        return UUID.randomUUID().toString();
    }

    /**
     * 多轮对话请求体。
     */
    public static class MultiTurnRequest {
        private String sessionId;
        private String message;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
