-- 对话记忆持久化表
-- 主键为 (MEMORY_ID, SEQ) 复合主键，SEQ 在每个 memoryId 内从 1 开始递增
CREATE TABLE IF NOT EXISTS MEMORY_MESSAGES (
    MEMORY_ID    VARCHAR(255) NOT NULL,
    SEQ          BIGINT       NOT NULL,
    MESSAGE_TYPE VARCHAR(100) NOT NULL,
    MESSAGE_JSON LONGTEXT     NOT NULL,
    PRIMARY KEY (MEMORY_ID, SEQ)
);

-- 按 memory_id + seq 排序查询时用到。重复启动时如果索引已存在会报错，但 continue-on-error=true 会忽略。
CREATE INDEX IDX_MEMORY_MESSAGES_SEQ ON MEMORY_MESSAGES (MEMORY_ID, SEQ);
