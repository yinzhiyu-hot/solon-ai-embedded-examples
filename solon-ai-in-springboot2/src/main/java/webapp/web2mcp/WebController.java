package webapp.web2mcp;

import org.noear.solon.ai.annotation.ResourceMapping;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.server.IMcpServerEndpoint;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.annotation.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * webapi 与 mcp-server 共用代码（控制器上，分别加上各自的注解）
 *
 * //ps: 添加 IMcpServerEndpoint 才会被 McpServerConfig 发布为 mcp 端点服务
 */
@McpServerEndpoint(channel = McpChannel.STREAMABLE, mcpEndpoint = "mcp/web/")
@RequestMapping("web/api")
@RestController
public class WebController implements IMcpServerEndpoint {
    @ToolMapping(description = "查询天气预报")
    @RequestMapping("getWeather")
    public String getWeather(@Param(description = "城市") String city) {
        return "晴，14度";
    }

    @ResourceMapping(uri = "config://app-version", description = "获取应用版本号")
    @RequestMapping("getAppVersion")
    public String getAppVersion() {
        return "v3.2.0";
    }


    @RequestMapping("/hello")
    public String hello(String name) {
//        //动态获取（添加工具等）//只是示例，可以删掉
//        McpServerEndpointProvider serverEndpointProvider = Solon.context().getBean("demo1");
//
//        serverEndpointProvider.addTool(new FunctionToolDesc(null));
//        serverEndpointProvider.addResource(new FunctionResourceDesc(null));
//        serverEndpointProvider.addPrompt(new FunctionPromptDesc(null));

        return "hello world: " + name;
    }
}