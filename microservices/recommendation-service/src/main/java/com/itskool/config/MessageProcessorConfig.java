package com.itskool.config;

import com.itskool.dto.RecommendationDto;
import com.itskool.event.Event;
import com.itskool.exceptions.EventProcessingException;
import com.itskool.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MessageProcessorConfig {
    private final RecommendationService recommendationService;

    @Bean
    public Consumer<Event<Long, RecommendationDto>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {
                case CREATE -> {
                    RecommendationDto recommendationDto = event.getData();
                    log.info("Create Recommendation with ID: {}", recommendationDto.getProductId());
                    recommendationService.createRecommendation(recommendationDto)
                            .block();
                }
                case DELETE -> {
                    Long productId = event.getKey();
                    log.info("Delete Recommendation with ProductID: {}", productId);
                    recommendationService.deleteRecommendations(productId)
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
