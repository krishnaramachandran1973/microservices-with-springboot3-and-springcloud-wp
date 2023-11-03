package com.itskool.service;

import com.itskool.domain.Product;
import com.itskool.dto.ProductDto;
import com.itskool.exceptions.InvalidInputException;
import com.itskool.exceptions.NotFoundException;
import com.itskool.mapper.ProductMapper;
import com.itskool.repository.ProductRepository;
import com.itskool.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {
    private final ServiceUtil util;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Mono<ProductDto> getProductById(Long productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.info("Will get product info for id={}", productId);
        
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(log.getName(), FINE)
                .map(productMapper::entityToDto)
                .map(this::setServiceAddress);
    }

    public Mono<ProductDto> createProduct(ProductDto productDto) {
        if (productDto.getProductId() < 1){
            throw new InvalidInputException("Invalid productId: " + productDto.getProductId());
        }
        Product productToCreate = productMapper.dtoToEntity(productDto);
        return productRepository.save(productToCreate)
                .log(log.getName(), FINE)
                .onErrorMap(DuplicateKeyException.class,e -> new InvalidInputException("Duplicate key, Product Id: " + productDto.getProductId()))
                .map(productMapper::entityToDto);

    }

    private ProductDto setServiceAddress(ProductDto e) {
        e.setServiceAddress(util.getServiceAddress());
        return e;
    }

    public Mono<Void> deleteProduct(Long productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);

        return productRepository.findProductByProductId(productId)
                .log(log.getName(), FINE)
                .flatMap(productRepository::delete);
    }
}
