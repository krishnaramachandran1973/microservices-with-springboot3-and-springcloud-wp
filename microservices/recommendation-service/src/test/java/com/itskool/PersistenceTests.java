package com.itskool;

import com.itskool.domain.Recommendation;
import com.itskool.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase{
    @Autowired
    private RecommendationRepository repository;

    private Recommendation savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
        Recommendation entity = Recommendation.builder()
                .productId(1L)
                .recommendationId(1L)
                .author("a")
                .rate(3)
                .content("c")
                .build();
        savedEntity = repository.save(entity).block();

        assertEqualsRecommendation(entity, savedEntity);
    }


    @Test
    void create() {
        Recommendation newEntity = Recommendation.builder()
                .productId(2L)
                .recommendationId(2L)
                .author("a")
                .rate(3)
                .content("c")
                .build();
        repository.save(newEntity).block();

        Recommendation foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    private void assertEqualsRecommendation(Recommendation expectedEntity, Recommendation actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(),           actualEntity.getAuthor());
        assertEquals(expectedEntity.getRate(),           actualEntity.getRate());
        assertEquals(expectedEntity.getContent(),          actualEntity.getContent());
    }

}
