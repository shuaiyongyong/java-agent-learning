package com.example.learning.service.springai;

import com.example.learning.record.response.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 综合工具服务（Spring AI 版），整合天气查询、数学计算、时间查询三大工具。
 * <p>
 * 每个方法标注 {@code @Tool}，Spring AI 会自动扫描并生成对应的 FunctionCallback，
 * 在对话时将工具暴露给大模型，模型根据用户意图自主选择调用哪个工具。
 */
@Slf4j
public class ComprehensiveToolService {

    // ==================== 天气查询工具 ====================

    /**
     * 获取指定城市的当前天气温度和状况。
     */
    @Tool(description = """
            获取指定城市的当前天气温度和状况。
            用法：传入城市中文名（如'北京'、'上海'、'神秘城'等任意城市名）。
            只要用户要求查询天气，必须调用此工具，不要直接用自然语言回复。
            如果工具执行报错或服务不可用，请告知用户暂时无法获取该城市天气，并建议用户稍后重试或查看其他天气来源。不要编造天气数据。
            """)
    public WeatherResponse getWeather(@ToolParam(description = "城市名称，如 '北京'") String city) {
        log.info("Spring AI 天气查询工具被调用，城市: {}", city);

        if ("神秘城".equalsIgnoreCase(city)) {
            log.error("查询天气失败: city={}", city);
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

    // ==================== 数学计算工具 ====================

    /**
     * 计算两个数的和。
     */
    @Tool(description = "计算两个数的和。用法：传入两个数字 a 和 b，返回它们的加法结果。")
    public double add(@ToolParam(description = "第一个加数") double a,
                      @ToolParam(description = "第二个加数") double b) {
        log.info("Spring AI 计算工具被调用: {} + {}", a, b);
        return a + b;
    }

    /**
     * 计算两个数的差。
     */
    @Tool(description = "计算两个数的差。用法：传入两个数字 a 和 b，返回 a 减去 b 的结果。")
    public double subtract(@ToolParam(description = "被减数") double a,
                           @ToolParam(description = "减数") double b) {
        log.info("Spring AI 计算工具被调用: {} - {}", a, b);
        return a - b;
    }

    /**
     * 计算两个数的积。
     */
    @Tool(description = "计算两个数的积。用法：传入两个数字 a 和 b，返回它们的乘法结果。")
    public double multiply(@ToolParam(description = "第一个因数") double a,
                           @ToolParam(description = "第二个因数") double b) {
        log.info("Spring AI 计算工具被调用: {} × {}", a, b);
        return a * b;
    }

    /**
     * 计算两个数的商。
     */
    @Tool(description = "计算两个数的商。用法：传入两个数字 a 和 b，返回 a 除以 b 的结果。除数为 0 时会报错。")
    public double divide(@ToolParam(description = "被除数") double a,
                         @ToolParam(description = "除数") double b) {
        log.info("Spring AI 计算工具被调用: {} ÷ {}", a, b);
        if (b == 0.0) {
            throw new ArithmeticException("除数不能为 0");
        }
        return a / b;
    }

    // ==================== 时间查询工具 ====================

    /**
     * 获取当前的日期和时间。
     */
    @Tool(description = "获取当前的日期和时间。调用时无需传入任何参数，返回格式为 'yyyy-MM-dd HH:mm:ss' 的中文时间字符串。")
    public String getCurrentTime() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("Spring AI 时间查询工具被调用，当前时间: {}", now);
        return now;
    }
}
