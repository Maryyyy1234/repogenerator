package com.example.repogenerator;

import com.example.repogenerator.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
@EnableConfigurationProperties(AppConfig.class)
@SpringBootApplication
public class RepogeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepogeneratorApplication.class, args);
	}

}
