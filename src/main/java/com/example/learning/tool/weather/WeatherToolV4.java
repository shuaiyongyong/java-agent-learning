package com.example.learning.tool.weather;

import com.example.learning.record.response.WeatherResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * V4: 极简型 — 一句话概括，最少文字
 */
@Service("weatherToolV4")
public class WeatherToolV4 {

    @Tool(description = "查询城市天气，传入城市名即可。")
    @dev.langchain4j.agent.tool.Tool("查询城市天气，传入城市名即可。")
    public WeatherResponse getWeather(
            @ToolParam(description = "城市名")
            @dev.langchain4j.agent.tool.P("城市名") String city) {
        logCall("V4", city);
        return mockWeather(city);
    }

    private void logCall(String variant, String city) {
        System.out.println("[ToolCall " + variant + "] city=" + city);
    }

    private WeatherResponse mockWeather(String city) {
        if ("神秘城".equalsIgnoreCase(city)) {
            throw new RuntimeException("天气服务暂时不可用：无法连接到气象数据源");
        }
        return switch (city) {
            case "北京" -> new WeatherResponse(city, "28°C", "晴");
            case "上海" -> new WeatherResponse(city, "26°C", "多云");
            case "广州" -> new WeatherResponse(city, "32°C", "雷阵雨");
            case "深圳" -> new WeatherResponse(city, "30°C", "小雨");
            default -> new WeatherResponse(city, "未知", "暂未收录该城市");
        };
    }
}
