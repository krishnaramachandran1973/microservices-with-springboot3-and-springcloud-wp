package com.itskool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);
        String mongoDbHost = ctx.getEnvironment()
                .getProperty("spring.data.mongodb.host");
        String mongoDbPort = ctx.getEnvironment()
                .getProperty("spring.data.mongodb.port");
        log.info("Connected to MongoDb: {} {}", mongoDbHost,mongoDbPort);
    }
}
