package com.itskool;

import com.itskool.properties.ReviewProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@EnableConfigurationProperties(ReviewProperties.class)
@SpringBootApplication
public class ReviewServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);
		String mysqlUri = ctx.getEnvironment()
				.getProperty("spring.datasource.url");
		log.info("Connected to MySQL: {}", mysqlUri);
	}

	@Bean
	public Scheduler jdbcScheduler(@Value("${app.thread-pool-size}") Integer threadPoolSize, @Value("${app.task-queue" +
			"-size}") Integer taskQueueSize){
		log.info("Creates a jdbcScheduler with thread pool size = {}", threadPoolSize);
		return Schedulers.newBoundedElastic(threadPoolSize,taskQueueSize,"jdbc-pool");
	}

}
