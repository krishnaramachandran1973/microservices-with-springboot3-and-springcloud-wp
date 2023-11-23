package com.itskool.controller;


import com.itskool.domain.ProductAggregate;
import com.itskool.domain.RecommendationSummary;
import com.itskool.domain.ReviewSummary;
import com.itskool.domain.ServiceAddresses;
import com.itskool.dto.ProductDto;
import com.itskool.dto.RecommendationDto;
import com.itskool.dto.ReviewDto;
import com.itskool.integration.ProductCompositeIntegration;
import com.itskool.tracing.ObservationUtil;
import com.itskool.util.ServiceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.logging.Level.FINE;
import static java.util.stream.Collectors.toList;

@SecurityRequirement(name = "security_auth")
@Tag(name = "ProductComposite", description = "REST API for composite product information.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("product-composite")
public class ProductCompositeController {
    private final ServiceUtil util;
    private final ProductCompositeIntegration integration;
    private final ObservationUtil observationUtil;
    private final SecurityContext nullSecCtx = new SecurityContextImpl();

    @Operation(
            summary = "${api.product-composite.get-composite-product.description}",
            description = "${api.product-composite.get-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @GetMapping("/{productId}")
    Mono<ProductAggregate> getProduct(@RequestHeader HttpHeaders requestHeaders, @PathVariable Long productId,
                                      @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
                                      @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent) {
        log.info("Will get composite product info for product.id={}", productId);
        HttpHeaders headers = getHeaders(requestHeaders, "X-group");
        return Mono.zip(
                        values -> createProductAggregate((ProductDto) values[0], (List<RecommendationDto>) values[1],
                                (List<ReviewDto>) values[2], util.getServiceAddress()),
                        integration.getProduct(headers, productId, delay, faultPercent),
                        integration.getRecommendations(headers, productId)
                                .collectList(),
                        integration.getReviews(headers, productId)
                                .collectList())
                .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                .log(log.getName(), FINE);
    }

    private HttpHeaders getHeaders(HttpHeaders requestHeaders, String... headers) {
        log.trace("Will look for {} headers: {}", headers.length, headers);
        HttpHeaders h = new HttpHeaders();
        for (String header : headers) {
            List<String> value = requestHeaders.get(header);
            if (value != null) {
                h.addAll(header, value);
            }
        }
        log.trace("Will transfer {}, headers: {}", h.size(), h);
        return h;
    }

    private ProductAggregate createProductAggregate(ProductDto product, List<RecommendationDto> recommendations,
                                                    List<ReviewDto> reviews, String serviceAddress) {

        // 1. Setup product info
        Long productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                                                              recommendations.stream()
                                                                      .map(r -> RecommendationSummary.builder()
                                                                              .recommendationId(r.getRecommendationId())
                                                                              .author(r.getAuthor())
                                                                              .content(r.getContent())
                                                                              .rate(r.getRate())
                                                                              .build())
                                                                      .collect(toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
                                              reviews.stream()
                                                      .map(r -> ReviewSummary.builder()
                                                              .reviewId(r.getReviewId())
                                                              .author(r.getAuthor())
                                                              .subject(r.getSubject())
                                                              .content(r.getContent())
                                                              .build())
                                                      .collect(toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0)
                .getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0)
                .getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }

    @Operation(
            summary = "${api.product-composite.create-composite-product.description}",
            description = "${api.product-composite.create-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping
    public Mono<Void> createProduct(@RequestBody ProductAggregate productAggregate) {
        return observationWithProductInfo(productAggregate.getProductId(), () -> createProductInternal(productAggregate));
    }

    private Mono<Void> createProductInternal(ProductAggregate productAggregate) {
        List<Mono<?>> monoList = new ArrayList<>();
        monoList.add(getLogAuthorizationInfoMono());

        log.info("Will create a new composite entity for product.id: {}", productAggregate.getProductId());
        ProductDto productDto = ProductDto.builder()
                .productId(productAggregate.getProductId())
                .name(productAggregate.getName())
                .weight(productAggregate.getWeight())
                .build();
        monoList.add(integration.createProduct(productDto));

        if (productAggregate.getRecommendations() != null) {
            productAggregate.getRecommendations()
                    .forEach(recommendationSummary -> {
                        RecommendationDto recommendationDto = RecommendationDto.builder()
                                .productId(productAggregate.getProductId())
                                .recommendationId(recommendationSummary.getRecommendationId())
                                .author(recommendationSummary.getAuthor())
                                .content(recommendationSummary.getContent())
                                .rate(recommendationSummary.getRate())
                                .build();
                        monoList.add(integration.createRecommendation(recommendationDto));
                    });
        }

        if (productAggregate.getReviews() != null) {
            productAggregate.getReviews()
                    .forEach(reviewSummary -> {
                        ReviewDto reviewDto = ReviewDto.builder()
                                .productId(productDto.getProductId())
                                .reviewId(reviewSummary.getReviewId())
                                .author(reviewSummary.getAuthor())
                                .subject(reviewSummary.getSubject())
                                .content(reviewSummary.getContent())
                                .build();
                        monoList.add(integration.createReview(reviewDto));
                    });
        }
        log.debug("createCompositeProduct: composite entities created for productId: {}", productAggregate.getProductId());

        return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
                .then();
    }

    private <T> T observationWithProductInfo(Long productInfo, Supplier<T> supplier) {
        return observationUtil.observe(
                "composite observation",
                "product info",
                "productId",
                String.valueOf(productInfo),
                supplier);
    }

    @Operation(
            summary = "${api.product-composite.delete-composite-product.description}",
            description = "${api.product-composite.delete-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/{productId}")
    public Mono<Void> deleteProduct(@PathVariable Long productId) {
        log.info("Will delete a product aggregate for product.id: {}", productId);
        return Mono.zip(
                        r -> "",
                        integration.deleteProduct(productId),
                        integration.deleteRecommendations(productId),
                        integration.deleteReviews(productId))
                .doOnError(ex -> log.warn("delete failed: {}", ex.toString()))
                .log(log.getName(), FINE)
                .then();
    }

    private Mono<SecurityContext> getLogAuthorizationInfoMono() {
        return getSecurityContextMono().doOnNext(this::logAuthorizationInfo);
    }

    private Mono<SecurityContext> getSecurityContextMono() {
        return ReactiveSecurityContextHolder.getContext()
                .defaultIfEmpty(nullSecCtx);
    }

    private void logAuthorizationInfo(SecurityContext sc) {
        if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwtToken = ((JwtAuthenticationToken) sc.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        }
        else {
            log.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            log.warn("No JWT supplied, running tests are we?");
        }
        else {
            if (log.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims()
                        .get("sub");
                Object scopes = jwt.getClaims()
                        .get("scope");
                Object expires = jwt.getClaims()
                        .get("exp");

                log.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject,
                        scopes, expires, issuer, audience);
            }
        }
    }
}
