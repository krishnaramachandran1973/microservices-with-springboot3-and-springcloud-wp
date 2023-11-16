package com.itskool;

import com.itskool.properties.ProductCompositeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(ProductCompositeProperties.class)
@SpringBootApplication
public class ProductCompositeServiceApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ProductCompositeServiceApplication.class, args);
    }

    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    public Scheduler publishEventScheduler(@Value("${app.thread-pool-size}") Integer threadPoolSize, @Value("${app" +
            ".task-queue" +
            "-size}") Integer taskQueueSize) {
        log.info("Creates a jdbcScheduler with thread pool size = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
    }

}
