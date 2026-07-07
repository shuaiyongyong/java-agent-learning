package com.example.learning.service.langchain;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

/**
 * 计算器工具类，供 LangChain4j {@code @AiService} 中的 Agent 自动调用。
 * <p>
 * 将此类注册为 Spring Bean 后，通过 {@code AgentAssistant} 的 {@code tools()} 参数注入，
 * LangChain4j 会在运行时扫描其中的 {@code @Tool} 标注方法并注册为可用工具。
 * 当用户提出数学计算问题时，模型会自动选择调用相应方法完成计算。
 *
 * <p>注意：LangChain4j 1.11.x 没有独立的 {@code @ToolParam} 注解，
 * 参数描述可通过 JavaDoc 或方法签名推断。
 */
@Service
public class CalculatorService {

    /**
     * 计算两个数的和
     *
     * @param a 第一个加数
     * @param b 第二个加数
     */
    @Tool("计算两个数的和")
    public double add(double a, double b) {
        return a + b;
    }

    /**
     * 计算两个数的差
     *
     * @param a 被减数
     * @param b 减数
     */
    @Tool("计算两个数的差")
    public double subtract(double a, double b) {
        return a - b;
    }

    /**
     * 计算两个数的积
     *
     * @param a 第一个因数
     * @param b 第二个因数
     */
    @Tool("计算两个数的积")
    public double multiply(double a, double b) {
        return a * b;
    }

    /**
     * 计算两个数的商，除数为 0 时抛出异常
     *
     * @param a 被除数
     * @param b 除数
     */
    @Tool("计算两个数的商")
    public double divide(double a, double b) {
        if (b == 0.0) {
            throw new ArithmeticException("除数不能为 0");
        }
        return a / b;
    }
}
