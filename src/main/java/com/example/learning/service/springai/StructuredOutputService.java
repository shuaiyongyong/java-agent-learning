package com.example.learning.service.springai;

import com.example.learning.record.Person;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Component
public class StructuredOutputService {

    private final ChatClient defaultClient;

    public StructuredOutputService(@Qualifier("defaultClient") ChatClient defaultClient) {
        this.defaultClient = defaultClient;
    }

    /**
     * ChatClient.entity(Class)
     * 代码更简洁，由框架自动处理 Prompt 构建和转换，是日常开发的首选。
     * */
    public Person extractPerson(String text) {
        return defaultClient.prompt()
                .user(u -> u.text("""
                        从以下文本中提取人物信息，只返回姓名和年龄。
                        文本: {text}
                        """)
                        .param("text", text))
                .call()
                .entity(Person.class);
    }

    /**
     * BeanOutputConverter
     * 提供了更底层的控制权，你可以手动构建 Prompt、自定义格式指令，或在不使用 ChatClient 的场景下（如直接使用 ChatModel）实现结构化输出。
     * */
    public Person extractPersonConverter(String text) {
        // 1. 创建 BeanOutputConverter，指定目标类型为 Person
        BeanOutputConverter<Person> converter = new BeanOutputConverter<>(Person.class);
        // 2. 构建用户提示词模板，预留 {format} 占位符
        String userText = """
                从以下文本中提取人物信息，只返回姓名和年龄。
                文本: {input}
                {format}
                """;
        // 3. 创建 Prompt，并将转换器的格式指令注入到 {format} 占位符
        Prompt prompt = new Prompt(
                PromptTemplate.builder()
                        .template(userText)
                        .variables(Map.of("input", text, "format", converter.getFormat()))
                        .build()
                        .createMessage()
        );
        // 4. 调用模型并获取响应
        String response = defaultClient.prompt(prompt).call().content();
        // 5. 使用转换器将模型响应解析为 Person 对象
        return converter.convert(response);
    }
}
