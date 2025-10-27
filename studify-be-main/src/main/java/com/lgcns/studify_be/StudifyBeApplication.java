package com.lgcns.studify_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
public class StudifyBeApplication {
    public static void main(String[] args) {
		Dotenv env = Dotenv.configure().ignoreIfMissing().load();
		env.entries().forEach(entry -> 
			System.setProperty(entry.getKey(), entry.getValue())
		);
        SpringApplication.run(StudifyBeApplication.class, args);
    }
}
