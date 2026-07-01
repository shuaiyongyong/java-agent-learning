package com.example.learning.service;

import com.example.learning.record.response.WeatherResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {
    // @Tool 注解会自动将该方法注册为一个 FunctionCallback，Spring AI 会扫描并生成对应的 JSON Schema 提供给模型。
    @Tool(description = "获取指定城市的当前天气温度和状况")
    public WeatherResponse getWeather(@ToolParam(description = "城市名称，如 '北京'") String city) {
        // Mock 数据，实际可调用第三方 API
        return switch (city) {
            case "北京" -> new WeatherResponse(city, "28°C", "晴");
            case "上海" -> new WeatherResponse(city, "26°C", "多云");
            default -> new WeatherResponse(city, "未知", "暂未收录该城市");
        };
    }

}
