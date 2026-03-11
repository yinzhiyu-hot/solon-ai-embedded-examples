package webapp.llm.tool;

import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.annotation.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import webapp.service.tool.PositionService;

@Component
public class PositionTools {
    @Autowired
    PositionService positionService;

    @ToolMapping(description = "城市位置")
    public String getPosition(@Param(description = "城市位置") String location) {
        return positionService.getPosition(location);
    }
}
