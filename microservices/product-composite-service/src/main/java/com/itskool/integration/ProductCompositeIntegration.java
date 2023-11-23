package com.itskool.integration;

import com.itskool.dto.ProductDto;
import com.itskool.dto.RecommendationDto;
import com.itskool.dto.ReviewDto;
import com.itskool.event.Event;
import com.itskool.exceptions.InvalidInputException;
import com.itskool.exceptions.NotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.net.URI;

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
            WebClient webClient,
            StreamBridge streamBridge,
            @Qualifier("publishEventScheduler")
                    Scheduler publishEventScheduler,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.review-service.host}") String reviewServiceHost) {

        this.webClient = webClient;
        this.streamBridge = streamBridge;
        this.publishEventScheduler = publishEventScheduler;
        this.productServiceUrl = "http://" + productServiceHost;
        this.recommendationServiceUrl = "http://" + recommendationServiceHost;
        this.reviewServiceUrl = "http://" + reviewServiceHost;
    }

    @Retry(name = "product")
    @TimeLimiter(name = "product")
    @CircuitBreaker(name = "product", fallbackMethod = "getProductFallbackValue")
    public Mono<ProductDto> getProduct(HttpHeaders headers, Long productId, int delay, int faultPercent) {
        URI url = UriComponentsBuilder
                .fromUriString(productServiceUrl + "/product/{productId}?delay={delay}&faultPercent={faultPercent}")
                .build(productId, delay, faultPercent);
        log.debug("Will call getProduct API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, error -> Mono.error(new NotFoundException("Product not found for id " + productId)))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, error -> Mono.error(new InvalidInputException(
                        "Product not found for id " + productId)))
                .bodyToMono(ProductDto.class);
    }

    private Mono<ProductDto> getProductFallbackValue(Long productId, int delay, int faultPercent,
                                                     CallNotPermittedException ex) {
        log.warn("Creating a fail-fast fallback product for productId = {}, delay = {}, faultPercent = {} and " +
                        "exception = {} ",
                productId, delay, faultPercent, ex.toString());

        if (productId == 13) {
            String errMsg = "Product Id: " + productId + " not found in fallback cache!";
            log.warn(errMsg);
            throw new NotFoundException(errMsg);
        }

        return Mono.just(ProductDto.builder()
                .productId(productId)
                .name("Fallback product" + productId)
                .weight(0)
                .serviceAddress("tempAddress")
                .build());
    }

    public Flux<RecommendationDto> getRecommendations(HttpHeaders headers, Long productId) {
        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        log.debug("Will call getRecommendations API on URL: {}", url);

        return this.webClient.get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToFlux(RecommendationDto.class)
                .onErrorResume(ex -> Flux.empty());
    }

    public Flux<ReviewDto> getReviews(HttpHeaders headers, Long productId) {
        String url = reviewServiceUrl + "/review?productId=" + productId;
        log.debug("Will call getReviews API on URL: {}", url);

        return this.webClient.get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
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
