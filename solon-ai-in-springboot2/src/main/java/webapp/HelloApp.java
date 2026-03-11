package webapp;

import org.noear.solon.Solon;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HelloApp {
    public static void main(String[] args) {
        if (Solon.app() != null) {
            return;
        }

        SpringApplication.run(HelloApp.class, args);
    }
}
