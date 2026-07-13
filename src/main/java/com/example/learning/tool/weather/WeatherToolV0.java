package com.example.learning.tool.weather;

import com.example.learning.record.response.WeatherResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * V0: 基准描述（原始措辞）— 功能导向，中规中矩
 */
@Service("weatherToolV0")
public class WeatherToolV0 {

    @Tool(description = """
            获取指定城市的当前天气温度和状况。
            用法：传入城市中文名（如'北京'、'上海'、'神秘城'等任意城市名）。
            只要用户要求查询天气，必须调用此工具，不要直接用自然语言回复。
            如果工具执行报错或服务不可用，请告知用户暂时无法获取该城市天气，并建议用户稍后重试或查看其他天气来源。不要编造天气数据。
            """)
    @dev.langchain4j.agent.tool.Tool("获取指定城市的当前天气温度和状况，传入城市中文名。只要用户要求查询天气就必须调用。")
    public WeatherResponse getWeather(
            @ToolParam(description = "城市名称，如 '北京'")
            @dev.langchain4j.agent.tool.P("城市名称，如 '北京'") String city) {
        logCall("V0", city);
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
