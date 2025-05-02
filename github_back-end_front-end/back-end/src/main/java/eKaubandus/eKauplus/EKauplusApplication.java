package eKaubandus.eKauplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EKauplusApplication {

	public static void main(String[] args) {
		SpringApplication.run(EKauplusApplication.class, args);
	}

}
