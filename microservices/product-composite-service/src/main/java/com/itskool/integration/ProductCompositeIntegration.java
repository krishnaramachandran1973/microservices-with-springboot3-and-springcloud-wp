package com.itskool.integration;

import com.itskool.dto.ProductDto;
import com.itskool.dto.RecommendationDto;
import com.itskool.dto.ReviewDto;
import com.itskool.event.Event;
import com.itskool.exceptions.InvalidInputException;
import com.itskool.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static com.itskool.event.Event.Type.CREATE;
import static com.itskool.event.Event.Type.DELETE;

@Slf4j
@Component
public class ProductCompositeIntegration {
    private final WebClient webClient;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;
    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;

    public ProductCompositeIntegration(
            WebClient.Builder webClientBuilder,
            StreamBridge streamBridge,
            @Qualifier("publishEventScheduler")
                    Scheduler publishEventScheduler,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.review-service.host}") String reviewServiceHost) {

        this.webClient = webClientBuilder.build();
        this.streamBridge = streamBridge;
        this.publishEventScheduler = publishEventScheduler;
        this.productServiceUrl = "http://" + productServiceHost;
        this.recommendationServiceUrl = "http://" + recommendationServiceHost;
        this.reviewServiceUrl = "http://" + reviewServiceHost;
    }

    public Mono<ProductDto> getProduct(Long productId) {
        String url = productServiceUrl + "/product/" + productId;
        log.debug("Will call getProduct API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, error -> Mono.error(new NotFoundException("Product not found for id " + productId)))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, error -> Mono.error(new InvalidInputException(
                        "Product not found for id " + productId)))
                .bodyToMono(ProductDto.class);
    }

    public Flux<RecommendationDto> getRecommendations(Long productId) {
        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        log.debug("Will call getRecommendations API on URL: {}", url);

        return this.webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(RecommendationDto.class)
                .onErrorResume(ex -> Flux.empty());
    }

    public Flux<ReviewDto> getReviews(Long productId) {
        String url = reviewServiceUrl + "/review?productId=" + productId;
        log.debug("Will call getReviews API on URL: {}", url);

        return this.webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(ReviewDto.class)
                .onErrorResume(ex -> Flux.empty());
    }

    public Mono<ProductDto> createProduct(ProductDto productDto) {
        return Mono.fromCallable(() -> {
                    sendMessage("products-out-0", new Event<>(CREATE, productDto.getProductId(), productDto));
                    return productDto;
                })
                .subscribeOn(publishEventScheduler);
    }

    public Mono<RecommendationDto> createRecommendation(RecommendationDto recommendationDto) {
        return Mono.fromCallable(() -> {
                    sendMessage("recommendations-out-0", new Event<>(CREATE,
                            recommendationDto.getProductId(), recommendationDto));
                    return recommendationDto;
                })
                .subscribeOn(publishEventScheduler);
    }

    public Mono<ReviewDto> createReview(ReviewDto reviewDto) {
        return Mono.fromCallable(() -> {
                    sendMessage("reviews-out-0", new Event<>(CREATE,
                            reviewDto.getProductId(), reviewDto));
                    return reviewDto;
                })
                .subscribeOn(publishEventScheduler);
    }

    private void sendMessage(String bindingName, Event<Long, Object> event) {
        log.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message<Event<Long, Object>> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    public Mono<Void> deleteProduct(Long productId) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();

    }

    public Mono<Void> deleteRecommendations(Long productId) {
        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();
    }

    public Mono<Void> deleteReviews(Long productId) {
        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();
    }
}
