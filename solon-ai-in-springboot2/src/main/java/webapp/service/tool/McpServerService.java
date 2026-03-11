package webapp.service.tool;

import org.noear.solon.ai.chat.message.ChatMessage;
import org.noear.solon.annotation.Param;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

/**
 * MCP工具，Mcp服务，McpServerTool 服务示例
 */
@Service
public class McpServerService {
    public String getWeather(@Param(description = "城市位置") String location) {
        return "晴，14度";
    }

    public String getAppVersion() {
        return "v3.2.0";
    }

    public String getEmail(@Param(description = "用户Id") String user_id) {
        return user_id + "@example.com";
    }

    public Collection<ChatMessage> askQuestion(@Param(description = "主题") String topic) {
        return Arrays.asList(
                ChatMessage.ofUser("请解释一下'" + topic + "'的概念？")
        );
    }
}
