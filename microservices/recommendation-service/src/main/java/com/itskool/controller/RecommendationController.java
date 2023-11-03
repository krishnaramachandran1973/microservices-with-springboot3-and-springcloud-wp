package com.itskool.controller;

import com.itskool.domain.Recommendation;
import com.itskool.dto.RecommendationDto;
import com.itskool.mapper.RecommendationMapper;
import com.itskool.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("recommendation")
public class RecommendationController {
    private final RecommendationMapper recommendationMapper;
    private final RecommendationService recommendationService;

    @PostMapping
    public Mono<RecommendationDto> createRecommendation(@RequestBody RecommendationDto recommendationDto){
        return recommendationService.createRecommendation(recommendationDto);
    }

    @GetMapping
    public Flux<RecommendationDto> getRecommendations(@RequestParam(value = "productId", required = true) Long productId) {
        return recommendationService.getRecommendations(productId);
    }

    @DeleteMapping("/{productId}")
    public Mono<Void> deleteRecommendations(@PathVariable Long productId){
        return recommendationService.deleteRecommendations(productId);
    }
}
