package pl.edu.pjatk.mas.s29904.simplebroker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SimplebrokerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimplebrokerApplication.class, args);
	}

}
