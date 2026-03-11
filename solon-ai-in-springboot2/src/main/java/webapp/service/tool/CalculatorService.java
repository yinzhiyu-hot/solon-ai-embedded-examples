package webapp.service.tool;

import org.springframework.stereotype.Component;

/**
 * 聊天模型，计算器服务，CalculatorTools 服务示例
 */
@Component
public class CalculatorService {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public float divide(float a, float b) {
        return a / b;
    }
}
