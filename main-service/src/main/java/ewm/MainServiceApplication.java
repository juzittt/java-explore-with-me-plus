package ewm;

import client.StatsClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@SpringBootApplication
@ComponentScan(value = {"ewm", "client"})
public class MainServiceApplication {
    public static void main(String[] args) {
       ConfigurableApplicationContext context =
               SpringApplication.run(MainServiceApplication.class, args);

        //Проверка работы клиента
        StatsClient statsClient = context.getBean(StatsClient.class);
        ResponseEntity<Object> response = statsClient.getStats(LocalDateTime.of(2021, 1, 1, 1, 1),
                LocalDateTime.of(2027, 1, 1, 1, 1),
                null,
                false);
        System.out.println(response.getBody());
    }
}
