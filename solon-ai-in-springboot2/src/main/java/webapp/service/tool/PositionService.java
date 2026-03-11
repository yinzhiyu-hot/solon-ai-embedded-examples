package webapp.service.tool;

import org.springframework.stereotype.Component;

/**
 * 聊天模型，位置服务，PositionTools 服务示例
 */
@Component
public class PositionService {

    public String getPosition(String location) {
        return "东经116.39742，北纬39.90923，北京";
    }
}
