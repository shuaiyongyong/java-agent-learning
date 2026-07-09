package com.example.learning.service.springai;

import com.example.learning.record.response.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Slf4j
public class SpringAiWeatherService {
    @Tool(description = """
            获取指定城市的当前天气温度和状况。
            用法：传入城市中文名（如"北京"、"上海"、"神秘城"等任意城市名）。
            只要用户要求查询天气，必须调用此工具，不要直接用自然语言回复。
            如果工具执行报错或服务不可用，请告知用户暂时无法获取该城市天气，并建议用户稍后重试或查看其他天气来源。不要编造天气数据。
            """)
    public WeatherResponse getWeather(@ToolParam(description = "城市名称，如 '北京'") String city) {
        // 模拟异常：对特定城市触发异常，用于观察 Agent 的错误处理能力
        if ("神秘城".equalsIgnoreCase(city)) {
            log.error("查询天气失败: city={}", city);
            throw new RuntimeException("天气服务暂时不可用：无法连接到气象数据源");
        }
        // Mock 数据，实际可调用第三方 API
        return switch (city) {
            case "北京" -> new WeatherResponse(city, "28°C", "晴");
            case "上海" -> new WeatherResponse(city, "26°C", "多云");
            default -> new WeatherResponse(city, "未知", "暂未收录该城市");
        };
    }

}
