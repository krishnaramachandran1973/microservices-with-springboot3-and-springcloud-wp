package com.itskool.integration;

import com.itskool.dto.ProductDto;
import com.itskool.dto.RecommendationDto;
import com.itskool.dto.ReviewDto;
import com.itskool.exceptions.InvalidInputException;
import com.itskool.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProductCompositeIntegration {
    private final WebClient webClient;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    public ProductCompositeIntegration(
            WebClient webClient,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort) {

        this.webClient = webClient;
        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    public Mono<ProductDto> getProduct(Long productId) {
        String url = productServiceUrl + "/" + productId;
        log.debug("Will call getProduct API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, error -> Mono.error(new NotFoundException("Product not found for id " + productId)))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals,error -> Mono.error(new InvalidInputException(
                        "Product not found for id " + productId)))
                .bodyToMono(ProductDto.class);
    }

    public Flux<RecommendationDto> getRecommendations(Long productId) {
        String url = recommendationServiceUrl + "?productId=" + productId;
        log.debug("Will call getRecommendations API on URL: {}", url);

        return this.webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        error -> Mono.error(new NotFoundException("No recommendations found for product with id " + productId)))
                .bodyToFlux(RecommendationDto.class)
                .switchIfEmpty(Flux.empty());
    }

    public Flux<ReviewDto> getReviews(Long productId) {
        String url = reviewServiceUrl + "?productId=" + productId;
        log.debug("Will call getReviews API on URL: {}", url);

        return this.webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        error -> Mono.error(new NotFoundException("No reviews found for product with id " + productId)))
                .bodyToFlux(ReviewDto.class)
                .switchIfEmpty(Flux.empty());
    }

    public Mono<ProductDto> createProduct(ProductDto productDto) {
        return webClient.post()
                .uri(productServiceUrl)
                .bodyValue(productDto)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals,error -> Mono.error(new InvalidInputException(
                        "Couldn't create Product with id" + productDto.getProductId())))
                .bodyToMono(ProductDto.class);
    }

    public Mono<RecommendationDto> createRecommendation(RecommendationDto recommendationDto) {
        return webClient.post()
                .uri(recommendationServiceUrl)
                .bodyValue(recommendationDto)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals,error -> Mono.error(new InvalidInputException(
                        "Couldn't create Recommendation for productId" + recommendationDto.getProductId())))
                .bodyToMono(RecommendationDto.class);
    }

    public Mono<ReviewDto> createReview(ReviewDto reviewDto) {
        return webClient.post()
                .uri(reviewServiceUrl)
                .bodyValue(reviewDto)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals,error -> Mono.error(new InvalidInputException(
                        "Couldn't create Review for productId" + reviewDto.getProductId())))
                .bodyToMono(ReviewDto.class);
    }

    public Mono<Void> deleteProduct(Long productId) {
        String url = productServiceUrl + "/" + productId;
        return webClient.delete().uri(url)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals,error -> Mono.error(new InvalidInputException(
                        "Couldn't delete Product with productId" + productId)))
                .bodyToMono(Void.class);

    }

    public Mono<Void> deleteRecommendations(Long productId) {
        String url = recommendationServiceUrl + "/" + productId;
        return webClient.delete().uri(url)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals,error -> Mono.error(new InvalidInputException(
                        "Couldn't delete Recommendations for Product with productId" + productId)))
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteReviews(Long productId) {
        String url = reviewServiceUrl + "/" + productId;
        return webClient.delete().uri(url)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals,error -> Mono.error(new InvalidInputException(
                        "Couldn't delete Reviews for Product with productId" + productId)))
                .bodyToMono(Void.class);
    }
}
