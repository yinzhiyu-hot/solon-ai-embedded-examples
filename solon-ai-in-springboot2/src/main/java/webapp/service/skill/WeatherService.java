package webapp.service.skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 天气查询服务，WeatherSkill 服务示例
 */
@Component
public class WeatherService {
    private static Logger log = LoggerFactory.getLogger(WeatherService.class);

    public String getWeather(String location) {
        log.error("进来了...");

        return "晴，14度";
    }
}
