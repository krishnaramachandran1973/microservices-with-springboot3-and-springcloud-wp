package com.itskool;

import com.itskool.domain.Product;
import com.itskool.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase{
    @Autowired
    private ProductRepository repository;

    private Product savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
        Product entity = Product.builder()
                .productId(1L)
                .name("a")
                .weight(3)
                .build();
        savedEntity = repository.save(entity).block();

        assertEqualsRecommendation(entity, savedEntity);
    }


    @Test
    void create() {
        Product newEntity = Product.builder()
                .productId(2L)
                .name("b")
                .weight(3)
                .build();
        repository.save(newEntity).block();

        Product foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    private void assertEqualsRecommendation(Product expectedEntity, Product actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());
    }
}
