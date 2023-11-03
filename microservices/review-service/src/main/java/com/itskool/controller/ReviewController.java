package com.itskool.controller;

import com.itskool.dto.ReviewDto;
import com.itskool.service.ReviewService;
import com.itskool.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("review")
public class ReviewController {
    private final ServiceUtil serviceUtil;
    private final ReviewService reviewService;

    @PostMapping
    public Mono<ReviewDto> createReview(@RequestBody ReviewDto reviewDto){
            return reviewService.createReview(reviewDto);
    }

    @GetMapping
    public Flux<ReviewDto> getReviews(@RequestParam(value = "productId", required = true) Long productId) {
        return reviewService.getReviews(productId);
    }

    @DeleteMapping("/{productId}")
    public Mono<Void> deleteReviews(@PathVariable Long productId){
        return reviewService.deleteReviews(productId);
    }
}
