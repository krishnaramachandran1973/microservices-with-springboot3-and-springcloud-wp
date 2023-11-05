package com.itskool.properties;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
@ConfigurationPropertiesScan
public class ProductCompositeProperties {
    private ProductService productService = new ProductService();
    private RecommendationService recommendationService = new RecommendationService();
    private ReviewService reviewService = new ReviewService();
    private Integer threadPoolSize;
    private Integer taskQueueSize;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProductService {
        private String host;
        private String port;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RecommendationService {
        private String host;
        private String port;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReviewService {
        private String host;
        private String port;
    }
}
