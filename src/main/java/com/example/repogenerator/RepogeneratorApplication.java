package com.example.repogenerator;

import com.example.repogenerator.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class RepogeneratorApplication {
	public static void main(String[] args) {
		SpringApplication.run(RepogeneratorApplication.class, args);
	}
}
