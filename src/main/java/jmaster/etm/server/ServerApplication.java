package jmaster.etm.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"jmaster.etm.server", "jmaster"})
@EntityScan(basePackages = {"jmaster.etm.server", "jmaster"})
@EnableJpaRepositories(basePackages = {"jmaster.etm.server", "jmaster"})
@EnableScheduling
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
