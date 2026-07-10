package com.example.learning.tool.weather;

import com.example.learning.record.response.WeatherResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * V1: 明确指令型 — 强语气 + 负面约束 + 使用示例 + 触发条件
 */
@Service("weatherToolV1")
public class WeatherToolV1 {

    @Tool(description = """
            【天气查询工具】
            功能：查询全球任意城市的实时天气信息（温度、天气状况）。
            触发条件：只要用户提到"天气"、"气温"、"几度"、"下雨"、"刮风"等关键词，立即调用本工具。
            参数说明：
              - city: 城市名称（必填），支持中文或拼音，例如：北京、Shanghai、广州
            重要规则：
              1. 禁止用自然语言猜测天气！必须调用工具获取真实数据。
              2. 用户没说具体城市时，追问城市名称。
              3. 工具返回错误时，告知用户"暂时无法查询"，不要编造数据。
            示例对话：
              用户："北京今天天气怎么样？" → 调用 getWeather(city="北京")
              用户："上海冷不冷？" → 调用 getWeather(city="上海")
              用户："明天会下雨吗？" → 追问："请问您想查询哪个城市的天气？"
            """)
    public WeatherResponse getWeather(@ToolParam(description = "城市名称，支持中文或拼音，如'北京'、'Shanghai'") String city) {
        logCall("V1", city);
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
