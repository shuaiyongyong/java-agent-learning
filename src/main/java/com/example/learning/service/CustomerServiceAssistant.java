package com.example.learning.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 基于 LangChain4j 的客服助手。
 * <p>
 * 通过 {@link AiService} 声明式生成实现，绑定到 Spring 容器中名为 {@code langchainOllamaChatModel} 的
 * 模型 Bean（在 {@code LangChain4jOllamaConfig} 中根据
 * {@code langchain4j.ollama.chat-model.*} 配置手动创建）。
 */
@AiService(wiringMode = EXPLICIT, chatModel = "langchainOllamaChatModel")
public interface CustomerServiceAssistant {

    @SystemMessage("""
            你是「悦购商城」的在线客服助手，名叫小悦。
            职责：解答订单、物流、退换货、优惠券与账户相关的问题。
            要求：
            1. 始终使用简体中文，语气友好、耐心、专业，称呼用户为「亲」。
            2. 回答简洁清晰，必要时分点说明操作步骤。
            3. 如果问题超出电商客服范围，或需要核实用户身份/订单等隐私信息，礼貌地引导用户联系人工客服（热线 400-100-1688）。
            4. 不要编造订单号、物流单号、优惠政策等具体信息；信息不足时主动向用户询问。
            """)
    String chat(String userMessage);
}
