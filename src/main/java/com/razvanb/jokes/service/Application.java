package com.razvanb.jokes.service;

import com.razvanb.jokes.service.model.properties.JokeLimitsProperties;
import com.razvanb.jokes.service.model.properties.RateLimitProperties;
import com.razvanb.jokes.service.model.properties.RetryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JokeLimitsProperties.class, RetryProperties.class, RateLimitProperties.class})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
