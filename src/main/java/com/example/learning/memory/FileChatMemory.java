package com.example.learning.memory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于文件的 Spring AI ChatMemory 实现。
 * <p>
 * 每个 sessionId 对应一个 JSON 文件，存储在配置目录下（默认 data/chat-memory/）。
 * 文件内容为消息对象的 JSON 数组，通过 {@link ObjectMapper} 序列化/反序列化。
 * <p>
 * 并发安全：使用 {@link ReentrantReadWriteLock} 保证同一 sessionId 的文件操作串行化。
 * 支持 maxMessages 滑动窗口——写入时自动裁剪超出窗口的历史消息。
 */
@Slf4j
public class FileChatMemory implements org.springframework.ai.chat.memory.ChatMemory {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEFAULT_DIR = "data/chat-memory";

    private final Path storeDir;
    private final int maxMessages;
    private final Map<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    /**
     * 默认构造，使用 data/chat-memory/ 目录和最大 100 条消息窗口。
     */
    public FileChatMemory() {
        this(DEFAULT_DIR);
    }

    /**
     * 指定存储目录的构造器。
     */
    public FileChatMemory(String dir) {
        this(dir, 100);
    }

    /**
     * 指定存储目录和消息窗口的构造器。
     *
     * @param dir         持久化目录
     * @param maxMessages 滑动窗口大小，负数或零表示不限制
     */
    public FileChatMemory(String dir, int maxMessages) {
        if (maxMessages <= 0) maxMessages = Integer.MAX_VALUE;
        this.maxMessages = maxMessages;
        this.storeDir = Path.of(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storeDir);
        } catch (IOException e) {
            log.error("无法创建记忆存储目录: {}", this.storeDir, e);
            throw new IllegalStateException("Cannot create memory store directory", e);
        }
        log.info("FileChatMemory initialized at: {}, maxMessages={}", this.storeDir, this.maxMessages);
    }

    // ==================== ChatMemory 接口实现 ====================

    /**
     * 获取会话的所有消息（不裁剪窗口，由 PromptChatMemoryAdvisor 负责 maxMessages 裁剪）。
     */
    @Override
    public List<Message> get(String conversationId) {
        return loadAllMessages(conversationId);
    }

    /**
     * 将一批新消息追加到会话中。
     * <p>
     * 先读取现有消息，追加新消息后，保持 maxMessages 窗口裁剪，再写回文件。
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) return;

        ReentrantReadWriteLock lock = getLock(conversationId);
        lock.writeLock().lock();
        try {
            List<MessageRecord> records = readRecordsFromFile(conversationId);
            for (Message msg : messages) {
                records.add(toRecord(msg));
            }
            // 滑动窗口裁剪
            if (records.size() > maxMessages) {
                records = records.subList(records.size() - maxMessages, records.size());
            }
            writeRecordsToFile(conversationId, records);
        } catch (IOException e) {
            log.error("保存消息失败: session={}", conversationId, e);
            throw new RuntimeException("Failed to save chat messages", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 清除指定会话的全部记忆。
     */
    @Override
    public void clear(String conversationId) {
        ReentrantReadWriteLock lock = getLock(conversationId);
        lock.writeLock().lock();
        try {
            Files.deleteIfExists(filePath(conversationId));
            log.debug("清除了 session={} 的记忆文件", conversationId);
        } catch (IOException e) {
            log.warn("删除记忆文件失败: session={}", conversationId, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== 内部方法 ====================

    private ReentrantReadWriteLock getLock(String conversationId) {
        return locks.computeIfAbsent(conversationId, k -> new ReentrantReadWriteLock());
    }

    private Path filePath(String conversationId) {
        return storeDir.resolve(sanitizeFilename(conversationId) + ".json");
    }

    /**
     * 加载会话所有消息（不裁剪窗口）。
     */
    private List<Message> loadAllMessages(String conversationId) {
        ReentrantReadWriteLock lock = getLock(conversationId);
        lock.readLock().lock();
        try {
            Path file = filePath(conversationId);
            if (!Files.exists(file)) {
                return Collections.emptyList();
            }
            String json = readFile(file);
            if (json == null || json.isBlank()) {
                return Collections.emptyList();
            }
            MessageRecord[] records = parseArray(json);
            List<Message> result = new ArrayList<>(records.length);
            for (MessageRecord r : records) {
                result.add(fromRecord(r));
            }
            return result;
        } catch (Exception e) {
            log.warn("读取消息文件失败: {}", filePath(conversationId), e);
            return Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 将消息列表序列化为 DTO 数组并写回文件。
     */
    private void writeRecordsToFile(String conversationId, List<MessageRecord> records) throws IOException {
        String json = serialize(records.toArray(new MessageRecord[0]));
        Files.writeString(filePath(conversationId), json, StandardCharsets.UTF_8);
    }

    /**
     * 从文件读取为 MessageRecord 列表。
     */
    private List<MessageRecord> readRecordsFromFile(String conversationId) throws IOException {
        Path file = filePath(conversationId);
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        String json = readFile(file);
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        MessageRecord[] records = parseArray(json);
        return new ArrayList<>(Arrays.asList(records));
    }

    private String readFile(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    private String serialize(MessageRecord[] records) throws IOException {
        return MAPPER.writeValueAsString(records);
    }

    private MessageRecord[] parseArray(String json) throws IOException {
        return MAPPER.readValue(json, MessageRecord[].class);
    }

    // ==================== 消息 ↔ DTO 转换 ====================

    /**
     * 将 Spring AI Message 转换为可序列化的 DTO。
     */
    private MessageRecord toRecord(Message msg) {
        MessageType type = msg.getMessageType();
        String text = extractText(msg);
        return new MessageRecord(type.name(), text);
    }

    /**
     * 从文本 Message 中提取纯文本内容。
     */
    private String extractText(Message msg) {
        if (msg instanceof SystemMessage m) return m.getText();
        if (msg instanceof UserMessage m) return m.getText();
        if (msg instanceof AssistantMessage m) return Optional.ofNullable(m.getText()).orElse("");
        return msg.getText();
    }

    /**
     * 从 DTO 恢复为 Spring AI Message 实例。
     */
    private Message fromRecord(MessageRecord record) {
        String type = record.type();
        String text = record.text();
        return switch (type) {
            case "SYSTEM" -> new SystemMessage(text);
            case "USER" -> new UserMessage(text);
            case "ASSISTANT", "AI" -> new AssistantMessage(text);
            default -> new UserMessage(text);
        };
    }

    /**
     * 防止路径遍历的安全文件名处理。
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\-_\\.]", "_");
    }

    // ==================== DTO 内部类 ====================

    /**
     * 用于 JSON 序列化的消息记录。仅存储 type 和 text，覆盖绝大多数对话场景。
     */
    record MessageRecord(String type, String text) {
        @JsonCreator
        MessageRecord(@JsonProperty("type") String type, @JsonProperty("text") String text) {
            this.type = type != null ? type : "USER";
            this.text = text != null ? text : "";
        }
    }
}
