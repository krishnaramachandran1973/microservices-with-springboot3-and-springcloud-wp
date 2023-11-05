package com.itskool.controller;

import com.itskool.dto.RecommendationDto;
import com.itskool.service.RecommendationService;
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
@RequestMapping("recommendation")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping
    public Flux<RecommendationDto> getRecommendations(@RequestParam(value = "productId", required = true) Long productId) {
        return recommendationService.getRecommendations(productId);
    }
}
