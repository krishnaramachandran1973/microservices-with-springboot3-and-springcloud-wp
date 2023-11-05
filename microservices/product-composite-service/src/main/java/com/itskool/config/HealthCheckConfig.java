package com.itskool.config;

import com.itskool.integration.ProductCompositeIntegration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class HealthCheckConfig {
    private final ProductCompositeIntegration integration;

    @Bean
    ReactiveHealthContributor healthContributor() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("product", integration::getProductHealth);
        registry.put("recommendation", integration::getRecommendationHealth);
        registry.put("review", integration::getReviewHealth);

        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
