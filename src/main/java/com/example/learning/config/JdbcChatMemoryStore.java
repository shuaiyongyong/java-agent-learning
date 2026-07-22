package com.example.learning.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.JacksonChatMessageJsonCodec;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 JDBC（MySQL 数据库）的 ChatMemoryStore 实现。
 * <p>
 * 替代原来的 ConcurrentHashMap 实现，使对话消息持久化到磁盘。
 * 应用重启后，MessageWindowChatMemory 会从数据库加载历史消息，
 * 实现跨会话的记忆恢复。
 * <p>
 * 表结构：MEMORY_MESSAGES (MEMORY_ID VARCHAR, SEQ BIGINT, MESSAGE_TYPE VARCHAR, MESSAGE_JSON CLOB)
 * 每条消息一条记录，按 SEQ 排序还原对话顺序。
 * <p>
 * 序列化使用 LangChain4j 内置的 {@code JacksonChatMessageJsonCodec}，
 * 确保与框架内部的 ChatMessage 格式完全兼容。
 */
@Slf4j
@Component
public class JdbcChatMemoryStore implements ChatMemoryStore {

    private final JdbcTemplate jdbcTemplate;
    private final JacksonChatMessageJsonCodec codec = new JacksonChatMessageJsonCodec();

    public JdbcChatMemoryStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatMessage> getMessages(Object memoryId) {
        String sql = "SELECT MESSAGE_JSON FROM MEMORY_MESSAGES WHERE MEMORY_ID = ? ORDER BY SEQ";
        List<String> jsonList = jdbcTemplate.queryForList(sql, String.class, memoryId);

        if (jsonList == null || jsonList.isEmpty()) {
            return List.of();
        }

        // 将所有 JSON 字符串拼接后一次性反序列化
        String combinedJson = "[" + String.join(",", jsonList) + "]";
        return codec.messagesFromJson(combinedJson);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // 先删除旧消息
        jdbcTemplate.update("DELETE FROM MEMORY_MESSAGES WHERE MEMORY_ID = ?", memoryId);

        if (messages == null || messages.isEmpty()) {
            return;
        }

        // 确定下一个 SEQ 起始值（从 1 开始）
        Long maxSeq = jdbcTemplate.queryForObject(
                "SELECT MAX(SEQ) FROM MEMORY_MESSAGES WHERE MEMORY_ID = ?",
                Long.class, memoryId);
        long startSeq = maxSeq != null ? maxSeq + 1 : 1;

        // 使用 codec 将整个消息列表序列化为单个 JSON 数组
        String json = codec.messagesToJson(messages);
        List<String> messageJsons = extractIndividualJsons(json);

        String sql = "INSERT INTO MEMORY_MESSAGES (MEMORY_ID, SEQ, MESSAGE_TYPE, MESSAGE_JSON) VALUES (?, ?, ?, ?)";
        List<Object[]> batch = new java.util.ArrayList<>();
        for (int i = 0; i < messageJsons.size(); i++) {
            String msgJson = messageJsons.get(i);
            String messageType = extractType(msgJson);
            batch.add(new Object[]{memoryId, startSeq + i, messageType, msgJson});
        }

        jdbcTemplate.batchUpdate(sql, batch);
        log.debug("更新了 memoryId={} 的 {} 条消息到数据库", memoryId, messages.size());
    }

    @Override
    public void deleteMessages(Object memoryId) {
        jdbcTemplate.update("DELETE FROM MEMORY_MESSAGES WHERE MEMORY_ID = ?", memoryId);
        log.debug("清除了 memoryId={} 的对话记忆", memoryId);
    }

    /**
     * 将 JSON 数组字符串拆分为单个消息的 JSON 字符串列表。
     */
    private List<String> extractIndividualJsons(String jsonArray) {
        List<ChatMessage> messages = codec.messagesFromJson(jsonArray);
        return messages.stream()
                .map(codec::messageToJson)
                .toList();
    }

    /**
     * 从单条消息 JSON 中提取类型名（用于存储 MESSAGE_TYPE 列）。
     */
    private String extractType(String json) {
        int idx = json.indexOf("\"@class\"");
        if (idx == -1) return "unknown";
        int start = json.indexOf(':', idx) + 1;
        int quoteStart = json.indexOf('"', start);
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        return quoteEnd > quoteStart ? json.substring(quoteStart + 1, quoteEnd) : "unknown";
    }
}
