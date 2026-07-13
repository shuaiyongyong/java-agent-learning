package com.example.learning.tool.weather;

import com.example.learning.record.response.WeatherResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * V3: 结构化型 — 分段标题 + 参数表格 + 返回值说明，类似 API 文档
 */
@Service("weatherToolV3")
public class WeatherToolV3 {

    @Tool(description = """
            ## 工具名称：getWeather
            ## 功能概述：查询指定城市的实时天气信息

            ### 何时调用
            - 用户询问天气、温度、气温、穿衣建议等
            - 用户提及"今天/明天/后天 + 城市 + 天气/气温/温度"

            ### 参数定义
            | 参数名 | 类型 | 必填 | 说明 | 示例 |
            |--------|------|------|------|------|
            | city | String | 是 | 城市中文名或英文名 | "北京", "Beijing", "上海" |

            ### 返回值
            返回包含以下字段的对象：
            - city: 城市名称
            - temperature: 当前温度（如 "28°C"）
            - description: 天气描述（如 "晴"、"多云"、"雷阵雨"）

            ### 注意事项
            - 必须调用此工具，不可凭记忆回答天气问题
            - 不支持的城市将返回 "暂未收录该城市"
            - 服务异常时抛出 RuntimeException，请据此告知用户
            """)
    @dev.langchain4j.agent.tool.Tool("getWeather：查询指定城市的实时天气信息（city、temperature、description）。用户询问天气/温度/穿衣建议时必须调用，不可凭记忆回答。")
    public WeatherResponse getWeather(
            @ToolParam(description = "城市名称，支持中英文，如'北京'或'Beijing'")
            @dev.langchain4j.agent.tool.P("城市名称，支持中英文，如'北京'或'Beijing'") String city) {
        logCall("V3", city);
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
