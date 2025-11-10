package com.book.swap;

import com.book.swap.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableCaching
@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.book.swap.repository")
public class BookSwapApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookSwapApplication.class, args);
	}

}
