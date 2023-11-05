package com.itskool.config;

import com.itskool.dto.ProductDto;
import com.itskool.event.Event;
import com.itskool.exceptions.EventProcessingException;
import com.itskool.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MessageProcessorConfig {
    private final ProductService productService;

    @Bean
    public Consumer<Event<Long, ProductDto>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {
                case CREATE -> {
                    ProductDto productDto = event.getData();
                    log.info("Create product with ID: {}", productDto.getProductId());
                    productService.createProduct(productDto)
                            .block();
                }
                case DELETE -> {
                    Long productId = event.getKey();
                    log.info("Delete product with ProductID: {}", productId);
                    productService.deleteProduct(productId)
                            .block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }
            log.info("Message processing done!");
        };
    }
}
