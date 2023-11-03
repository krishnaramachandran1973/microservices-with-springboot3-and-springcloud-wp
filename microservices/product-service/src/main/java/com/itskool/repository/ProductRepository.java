package com.itskool.repository;

import com.itskool.domain.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product,String> {
   Mono<Product> findProductByProductId(Long productId);
}
