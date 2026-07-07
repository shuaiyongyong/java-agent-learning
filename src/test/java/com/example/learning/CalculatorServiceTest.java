package com.example.learning;

import com.example.learning.service.langchain.CalculatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试计算器工具类 {@code CalculatorService} 的基本功能。
 * <p>
 * 不依赖 Ollama / LLM，纯单元测试验证 @Tool 标注的方法逻辑是否正确。
 */
class CalculatorServiceTest {

    private final CalculatorService calculator = new CalculatorService();

    @Test
    void testAdd() {
        assertEquals(8.0, calculator.add(3, 5), 0.001);
        assertEquals(-2.0, calculator.add(3, -5), 0.001);
    }

    @Test
    void testSubtract() {
        assertEquals(7.0, calculator.subtract(10, 3), 0.001);
        assertEquals(-3.0, calculator.subtract(3, 6), 0.001);
    }

    @Test
    void testMultiply() {
        assertEquals(42.0, calculator.multiply(6, 7), 0.001);
        assertEquals(0.0, calculator.multiply(100, 0), 0.001);
    }

    @Test
    void testDivide() {
        assertEquals(25.0, calculator.divide(100, 4), 0.001);
        assertEquals(0.5, calculator.divide(1, 2), 0.001);
    }

    @Test
    void testDivideByZero() {
        assertThrows(ArithmeticException.class, () -> calculator.divide(1, 0));
    }
}
