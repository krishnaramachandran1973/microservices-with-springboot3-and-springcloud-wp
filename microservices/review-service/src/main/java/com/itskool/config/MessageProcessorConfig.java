package com.itskool.config;

import com.itskool.dto.ReviewDto;
import com.itskool.event.Event;
import com.itskool.exceptions.EventProcessingException;
import com.itskool.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MessageProcessorConfig {
    private final ReviewService reviewService;

    @Bean
    public Consumer<Event<Long, ReviewDto>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {
                case CREATE -> {
                    ReviewDto reviewDto = event.getData();
                    log.info("Create Review with ID: {}", reviewDto.getProductId());
                    reviewService.createReview(reviewDto)
                            .block();
                }
                case DELETE -> {
                    Long productId = event.getKey();
                    log.info("Delete Review with ProductID: {}", productId);
                    reviewService.deleteReviews(productId)
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
