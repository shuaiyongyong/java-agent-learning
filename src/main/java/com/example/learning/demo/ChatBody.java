package com.example.learning.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatBody {
    /**
     * 消息体
     */
    private List<ChatMessage> messages;
    /**
     * 模型
     */
    private String model;
    /**
     * 是否开启流式
     */
    private boolean stream;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ChatMessage implements Serializable {
        /**
         * 内容
         */
        private String content;
        /**
         * 角色
         */
        private String role;
    }
}
