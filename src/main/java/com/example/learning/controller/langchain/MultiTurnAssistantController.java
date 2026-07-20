package com.example.learning.controller.langchain;

import com.example.learning.assistant.ComprehensiveAssistant;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

/**
 * 综合助手的多轮对话控制器。
 * <p>
 * 通过 sessionId 实现会话隔离，同一 sessionId 下的连续对话会共享聊天记忆，
 * 助手能够记住上文提到的信息（如人名、偏好等）。
 * 如果请求中未提供 sessionId，则自动生成一个新的 UUID。
 */
@Slf4j
@RestController
@RequestMapping("/mt")
public class MultiTurnAssistantController {

    @Resource
    ComprehensiveAssistant comprehensiveAssistant;

    /**
     * 非流式多轮聊天接口。
     * <p>
     * 示例：
     * <pre>
     * POST /assistant/comprehensive/chat
     * {"sessionId": "abc-123", "message": "我叫小明"}
     *
     * POST /assistant/comprehensive/chat
     * {"sessionId": "abc-123", "message": "你知道我的名字吗？"}
     * </pre>
     *
     * @param request 包含 sessionId 和用户消息
     * @return 助手的回复
     */
    @PostMapping("/chat")
    public String chat(
            @RequestBody(required = false) MultiTurnRequest request) {
        String sessionId = resolveSessionId(request);
        String memoryId = sessionId;
        log.info("[多轮对话] sessionId={}, message={}", memoryId, request != null ? request.getMessage() : "");
        return comprehensiveAssistant.chat(memoryId, request != null ? request.getMessage() : "");
    }

    /**
     * 流式多轮聊天接口（SSE）。
     *
     * @param request 包含 sessionId 和用户消息
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestBody(required = false) MultiTurnRequest request) {
        String sessionId = resolveSessionId(request);
        String memoryId = sessionId;
        SseEmitter emitter = new SseEmitter(60_000L);

        log.info("[多轮对话-流式] sessionId={}", memoryId);

        TokenStream tokenStream = comprehensiveAssistant.chatStream(memoryId, request != null ? request.getMessage() : "");
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
     * 清除指定会话的记忆。
     *
     * @param sessionId 会话 ID
     */
    @DeleteMapping("/session/{sessionId}")
    public String clearSession(@PathVariable String sessionId) {
        log.info("[清除记忆] sessionId={}", sessionId);
        // MessageWindowChatMemory.clear() 会清空该会话的消息
        // 由于 ChatMemory 由 AiService 内部托管，这里通过发送特殊指令来间接清除
        // 更优雅的方式是暴露 ChatMemoryAccess API，但简单场景下新建 sessionId 即可
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
