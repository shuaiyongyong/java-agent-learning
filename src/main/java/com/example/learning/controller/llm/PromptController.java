package com.example.learning.controller.llm;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Spring AI 基础Prompt构造实战
 */
@RestController
public class PromptController {

    @Resource(name = "deepseek")
    private ChatModel deepseekChatModel;

    @Resource(name = "deepseekChatClient")
    private ChatClient deepseekChatClient;


    /**
     * Message 顺序：
     * 在new Prompt(userMessage, systemMessage)中，虽然参数顺序是 userMessage 在前，
     * 但 Spring AI 内部会自动调整顺序，确保 SystemMessage 在最前面。
     * 不过建议显式使用List.of(systemMessage, userMessage)来明确顺序，避免混淆。
     */

    /**
     * 方式1：ChatClient 链式调用，自动组装Prompt（推荐，简单场景）
     * 访问示例：http://localhost:8080/prompt/chat?question=JAVA介绍
     */
    @GetMapping("/prompt/chat")
    public Flux<String> chat(String question) {
        return deepseekChatClient.prompt()
                // 系统消息：设定AI能力边界
                .system("你是一个牙科助手，只回答牙科问题，其它问题回复：我只能回答牙科相关问题，其它无可奉告")
                .user(question)
                .stream()
                .content();
    }

    /**
     * 方式2：ChatModel 手动组装Message，返回完整ChatResponse（底层灵活，复杂场景）
     * 访问示例：http://localhost:8080/prompt/chat2?question=白小飞
     */
    @GetMapping("/prompt/chat2")
    public Flux<ChatResponse> chat2(String question) {
        // 1. 创建系统消息：设定AI角色和输出要求
        SystemMessage systemMessage = new SystemMessage("你是一个讲故事的助手，每个故事控制在300字以内");
        // 2. 创建用户消息：用户的具体提问
        UserMessage userMessage = new UserMessage(question);
        // 3. 封装成Prompt：注意SystemMessage必须放在前面
        Prompt prompt = new Prompt(userMessage, systemMessage);
        // 4. 调用大模型，返回完整ChatResponse（包含元数据）
        return deepseekChatModel.stream(prompt);
    }

    /**
     * 方式3：ChatModel 手动组装Message，流式返回纯文本（手动提取内容）
     * 访问示例：http://localhost:8080/prompt/chat3?question=白小飞
     */
    @GetMapping("/prompt/chat3")
    public Flux<String> chat3(String question) {
        SystemMessage systemMessage = new SystemMessage("你是一个讲故事的助手，每个故事控制在600字以内且以HTML格式返回");
        UserMessage userMessage = new UserMessage(question);
        Prompt prompt = new Prompt(userMessage, systemMessage);
        // 手动从ChatResponse中提取文本内容
        return deepseekChatModel.stream(prompt)
                .map(response -> response.getResults().get(0).getOutput().getText());
    }

    /**
     * 方式4：ChatClient 同步调用，获取AssistantMessage（包含元数据）
     * 访问示例：http://localhost:8080/prompt/chat4?question=白小飞
     */
    @GetMapping("/prompt/chat4")
    public String chat4(String question) {
        // 链式调用，获取完整ChatResponse，再提取AssistantMessage
        var assistantMessage = deepseekChatClient.prompt()
                .user(question)
                .call()
                .chatResponse()
                .getResult()
                .getOutput();
        return assistantMessage.getText();
    }
}