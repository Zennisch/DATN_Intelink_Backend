package intelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IntelinkServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelinkServerApplication.class, args);
    }

}
