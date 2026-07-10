package com.example.learning.tool.weather;

import com.example.learning.record.response.WeatherResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * V2: 场景化型 — 以用户场景切入，自然对话语气
 */
@Service("weatherToolV2")
public class WeatherToolV2 {

    @Tool(description = """
            想知道某个城市现在热不热、要不要带伞？用这个工具可以帮你查询任意城市的实时天气。
            比如用户问"北京今天多少度？"或者"上海天气怎么样"，只需要把城市名字传进来就能得到温度、天气状况等信息。
            不管是大城市还是小城镇，只要说出城市名都可以查。如果查不到，就诚实地告诉用户暂时没有找到这个城市的天气数据。
            注意：不要用你自己知道的天气信息来回答，一定要调用这个工具来获取最新数据。
            """)
    public WeatherResponse getWeather(@ToolParam(description = "要查询天气的城市名称，比如'北京'、'上海'、'广州'") String city) {
        logCall("V2", city);
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
