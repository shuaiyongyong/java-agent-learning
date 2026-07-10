package com.example.learning.controller.llm;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/deepseek")
public class DeepseekController {

    private final String weahterSystemV1 = "你是一位资深气象数据分析师，具备专业的气象学素养。你的核心职责是基于精准的天气工具查询结果，为用户提供严谨、详尽的气象解读。在回复中，需清晰阐述温度、湿度、气压、风速、降水量及能见度等核心指标，并注重数据的横向对比（如与昨日同期温差）。严禁凭空捏造天气信息，所有结论必须严格锚定工具返回的实时数据。对于极端或异常天气，需给出基于数据的专业风险提示，语言风格应客观、理性、精准。";

    private final String  weahterSystemV2 = "你是一位贴心的智能生活助理，擅长将枯燥的天气数据转化为实用的生活指南。当你调用天气工具获取信息后，不要仅播报数字，而是要主动为用户着想：结合早中晚时段分析体感温度，并给出相应的穿衣建议（如“早晚偏凉需加薄外套”）、洗车指数、紫外线防护提醒以及是否适宜开窗通风等。回复语气应亲切温暖、平易近人，像家人一样叮嘱，让天气信息真正服务于用户的每日生活决策。";

    private final String  weahterSystemV3 = "你是一名经验丰富的户外探险领队兼天气顾问，深知天气对户外活动（徒步、露营、骑行等）的决定性影响。你的侧重点在于评估天气对具体户外计划的可行性及安全风险。在输出时，需重点突出风力等级、降水概率、湿度变化以及突发对流天气的可能性，并明确给出“适宜出行”、“需备防雨装备”或“建议调整行程”等具体行动指令。回复必须直击痛点，措辞果断、清晰，将安全性和行程舒适度放在首位。";

    private final String  weahterSystemV4 = "你是一个高效的极简天气播报员，奉行“Less is More”的原则。当你获取天气工具的数据后，必须对冗余信息进行高度提炼，只输出用户最想知道的核心三要素：天气状况（晴/雨/阴等）、实时/预报温度、以及一条最显著的异常提示（如大风预警或强降温）。回复需控制在50字以内，拒绝寒暄和废话，禁止使用复杂表格，采用“标题式”短语直接呈现结果，确保用户一眼就能获取关键信息。";

    private final String  weahterSystemV5 = "你是一个专为天气功能模块设计的测试助手。当你接收到用户查询时，首要动作是触发天气工具并获取原始JSON数据。在回复中，你需要以结构化的方式呈现结果，用于验证数据解析的准确性。输出格式需包含两部分：第一部分为【数据摘要】，按指定字段（地点、时间、温度、天气现象）提取关键值；第二部分为【逻辑校验】，主动检查并报告数据是否存在明显异常（如温度突变值、字段缺失等）。回复语气客观、中立，保持工程师式的严谨，不添加任何主观建议或修饰。";

    @Resource(name = "deepseek")
    private ChatModel deepseekChatModel;

    @Resource(name = "deepseekChatClient")
    private ChatClient deepseekChatClient;

    /**
     * ChatClient 链式调用
     */
    @GetMapping("/getWeather/chatClient/v1")
    public Flux<String> chatClientV1(String question) {
        return deepseekChatClient.prompt()
                .system(weahterSystemV1)
                .user(question)
                .stream()
                .content();
    }

    @GetMapping("/getWeather/chatClient/v2")
    public String chatClientV2(String question) {
        // 链式调用，获取完整ChatResponse，再提取AssistantMessage
        var assistantMessage = deepseekChatClient.prompt()
                .system(weahterSystemV2)
                .user(question)
                .call()
                .chatResponse()
                .getResult()
                .getOutput();
        return assistantMessage.getText();
    }

    /**
     * ChatModel 手动组装Message，返回完整ChatResponse（底层灵活，复杂场景）
     */
    @GetMapping("/getWeather/chatModel/v1")
    public Flux<String> chatModelV1(String question) {
        // 1. 创建系统消息：设定AI角色和输出要求
        SystemMessage systemMessage = new SystemMessage(weahterSystemV1);
        // 2. 创建用户消息：用户的具体提问
        UserMessage userMessage = new UserMessage(question);
        // 3. 封装成Prompt：注意SystemMessage必须放在前面
        Prompt prompt = new Prompt(userMessage, systemMessage);
        // 4. 调用大模型，返回完整ChatResponse（包含元数据）
        return deepseekChatModel.stream(prompt)
                .map(response -> response.getResults().get(0).getOutput().getText());
    }

    @GetMapping("/getWeather/chatModel/v2")
    public Flux<ChatResponse> chatModelV2(String question) {
        // 1. 创建系统消息：设定AI角色和输出要求
        SystemMessage systemMessage = new SystemMessage(weahterSystemV2);
        // 2. 创建用户消息：用户的具体提问
        UserMessage userMessage = new UserMessage(question);
        // 3. 封装成Prompt：注意SystemMessage必须放在前面
        Prompt prompt = new Prompt(userMessage, systemMessage);
        // 4. 调用大模型，返回完整ChatResponse（包含元数据）
        return deepseekChatModel.stream(prompt);
    }

}
