package com.example.learning.demo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Stream;

public class OllamaClient {
    // Ollama 服务的默认地址
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/chat";
    private static final ObjectMapper mapper = new ObjectMapper();


    public void send(ChatBody body){
        if(body.isStream()){
            streamSend(body);
        }else {
            noStreamSend(body);
        }
    }

    private void streamSend(ChatBody body) {
        String jsonBody = JSONObject.toJSONString(body);
        // 2. 创建 HttpClient
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)) // 设置连接超时
                .build();
        // 3. 创建 HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_API_URL))
                .timeout(Duration.ofSeconds(60)) // 设置整个请求的超时时间
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        // 4. 发送请求并获取流式响应行
        try (Stream<String> lines = client.send(request, HttpResponse.BodyHandlers.ofLines()).body()) {
            // 5. 迭代每一行
            lines.forEach(line -> {
                try {
                    JsonNode chunk = mapper.readTree(line);
                    // 提取 content 字段（注意是 message.content）
                    String content = chunk.path("message").path("content").asText();
                    if (!content.isEmpty()) {
                        System.out.print(content); // 实时打印，不换行
                        System.out.flush();
                    }
                    // 检查是否结束（done:true）
                    if (chunk.path("done").asBoolean(false)) {
                        System.out.println(); // 结束换行
                    }
                } catch (Exception e) {
                    System.err.println("解析 JSON 失败: " + line);
                }
            });
        } catch (Exception e) {
            System.err.println("请求异常: " + e.getMessage());
        }
    }

    private void noStreamSend(ChatBody body) {
        String jsonBody = JSONObject.toJSONString(body);
        // 2. 创建 HttpClient
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)) // 设置连接超时
                .build();
        // 3. 创建 HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_API_URL))
                .timeout(Duration.ofSeconds(60)) // 设置整个请求的超时时间
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        // 4. 发送请求并处理响应
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // 5. 打印结果
            if (response.statusCode() == 200) {
                try {
                    JsonNode chunk = mapper.readTree(response.body());
                    String content = chunk.path("message").path("content").asText();
                    if (!content.isEmpty()) {
                        System.out.print(content); // 实时打印，不换行
                        System.out.flush();
                    }
                    // 检查是否结束（done:true）
                    if (chunk.path("done").asBoolean(false)) {
                        System.out.println(); // 结束换行
                    }
                } catch (Exception e) {
                    System.err.println("解析 JSON 失败: " + response.body());
                }
            } else {
                System.err.println("请求失败，状态码: " + response.statusCode() + ";错误信息: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("请求异常: " + e.getMessage());
        }
    }
}
