package com.itskool.controller;

import com.itskool.dto.ReviewDto;
import com.itskool.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("review")
public class ReviewController {
    private final ReviewService reviewService;
    @GetMapping
    public Flux<ReviewDto> getReviews(@RequestParam(value = "productId", required = true) Long productId) {
        return reviewService.getReviews(productId);
    }
}
