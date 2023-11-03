package com.itskool.repository;

import com.itskool.domain.Recommendation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface RecommendationRepository extends ReactiveMongoRepository<Recommendation,String> {
    Flux<Recommendation> findRecommendationByProductId(Long productId);
}
